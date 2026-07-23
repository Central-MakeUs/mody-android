package com.makeus.mody.core.network.interceptor

import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.repository.SessionReauthenticator
import com.makeus.mody.core.domain.session.SessionExpiredNotifier
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.model.auth.TokenReissueRequest
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 401 응답 시 3단계로 세션을 살린다:
 *  1) refresh token 으로 access token 재발급 후 원 요청 재시도
 *  2) refresh 까지 만료면 소셜 SDK 세션으로 무음 재로그인([SessionReauthenticator]) 후 재시도
 *  3) 그것도 실패면 토큰 정리 + 세션 만료 이벤트 발행([SessionExpiredNotifier]) → 로그인 화면 유도
 *
 * 재발급 호출이 네트워크 오류 등 일시 장애로 실패한 경우는 세션을 건드리지 않고
 * 이번 요청만 포기한다(다음 요청에서 재시도).
 *
 * AuthApi/SessionReauthenticator 는 [Lazy] 로 주입해 OkHttp ↔ Retrofit DI 순환을 끊는다.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: Lazy<AuthApi>,
    private val sessionReauthenticator: Lazy<SessionReauthenticator>,
    private val sessionExpiredNotifier: SessionExpiredNotifier,
) : Authenticator {

    // 동시 401 재발급 직렬화. 한 번만 재발급하고 나머지는 갱신된 토큰으로 재시도.
    private val reissueLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // reissue 호출 자체가 401 이면 재귀 방지
        if (response.request.url.encodedPath == REISSUE_PATH) return null
        // 이미 한 번 재시도(재발급 후)한 요청이면 포기
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken.isBlank()) return null

        synchronized(reissueLock) {
            // 락 획득 사이 다른 스레드가 이미 재발급했으면, 실패한 요청의 토큰과
            // 현재 저장된 토큰이 다름 → 재발급 없이 최신 토큰으로 재시도.
            val currentAccess = runBlocking { tokenManager.getAccessToken() }
            val failedAuth = response.request.header("Authorization")
            if (currentAccess.isNotBlank() && failedAuth != "Bearer $currentAccess") {
                return response.request.retryWith(currentAccess)
            }

            // 1) 최신 refresh 로 재발급(락 안에서 한 번만).
            val latestRefresh = runBlocking { tokenManager.getRefreshToken() }
            if (latestRefresh.isBlank()) return null // 다른 스레드가 이미 세션을 정리함
            val reissueResult = runBlocking {
                runCatching { authApi.get().reissue(TokenReissueRequest(latestRefresh)) }
            }

            reissueResult.getOrNull()?.takeIf { it.isSuccess }?.result?.let { tokens ->
                runBlocking {
                    tokenManager.setAccessToken(tokens.accessToken)
                    tokenManager.setRefreshToken(tokens.refreshToken)
                }
                return response.request.retryWith(tokens.accessToken)
            }

            // 재발급 실패 분류: 401/403 또는 isSuccess=false 만 "refresh 만료"로 취급.
            // 그 외(네트워크 오류 등)는 일시 장애 → 세션 유지한 채 이번 요청만 포기.
            // reissue 는 ModyCallAdapter 를 거쳐 도메인 HttpResponseException 으로 던져지므로
            // retrofit HttpException 뿐 아니라 이쪽도 함께 분류해야 세션 정리가 동작한다.
            val refreshDead = when (val e = reissueResult.exceptionOrNull()) {
                null -> true // HTTP 200 인데 isSuccess=false → 서버가 refresh 거부
                is HttpException -> e.code() == 401 || e.code() == 403
                is HttpResponseException ->
                    e.status == HttpResponseStatus.Unauthorized || e.status == HttpResponseStatus.Forbidden
                else -> false
            }
            if (!refreshDead) return null

            // 2) 소셜 SDK 세션으로 무음 재로그인. 성공 시 새 JWT 가 저장돼 있다.
            val relogged = runBlocking {
                withTimeoutOrNull(SILENT_REAUTH_TIMEOUT_MS) {
                    runCatching { sessionReauthenticator.get().reauthenticate() }
                        .getOrDefault(false)
                } ?: false
            }
            if (relogged) {
                val newAccess = runBlocking { tokenManager.getAccessToken() }
                if (newAccess.isNotBlank()) return response.request.retryWith(newAccess)
            }

            // 3) 세션 완전 만료 → 토큰 정리 + 로그인 화면 유도.
            runBlocking { tokenManager.clear() }
            sessionExpiredNotifier.notifySessionExpired()
            return null
        }
    }

    private fun Request.retryWith(accessToken: String): Request =
        newBuilder().header("Authorization", "Bearer $accessToken").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val REISSUE_PATH = "/api/v1/auth/reissue"
        const val SILENT_REAUTH_TIMEOUT_MS = 3_000L
    }
}
