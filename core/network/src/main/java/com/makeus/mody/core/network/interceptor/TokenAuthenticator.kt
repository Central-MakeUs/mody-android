package com.makeus.mody.core.network.interceptor

import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.model.auth.TokenReissueRequest
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 401 응답 시 refresh token 으로 access token 재발급 후 원 요청을 재시도한다.
 * 재발급 실패(refresh 만료 등)거나 이미 재시도한 요청이면 포기(null) → 요청은 401 그대로.
 *
 * AuthApi 는 [Lazy] 로 주입해 OkHttp ↔ Retrofit ↔ AuthApi DI 순환을 끊는다.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: Lazy<AuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // reissue 호출 자체가 401 이면 재귀 방지
        if (response.request.url.encodedPath == REISSUE_PATH) return null
        // 이미 한 번 재시도(재발급 후)한 요청이면 포기
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken.isBlank()) return null

        val reissued = runBlocking {
            runCatching { authApi.get().reissue(TokenReissueRequest(refreshToken)) }.getOrNull()
        }
        val tokens = reissued?.takeIf { it.isSuccess }?.result ?: return null

        runBlocking {
            tokenManager.setAccessToken(tokens.accessToken)
            tokenManager.setRefreshToken(tokens.refreshToken)
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .build()
    }

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
    }
}
