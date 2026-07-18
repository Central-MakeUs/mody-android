package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.notification.PushTokenSynchronizer
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.PushTokenRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.model.auth.TokenLogoutRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionRepository: SessionRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val pushTokenSynchronizer: PushTokenSynchronizer,
) : AuthRepository {

    override suspend fun loginWithSocial(
        type: SocialLoginType,
        socialAccessToken: String,
    ): AuthStatus {
        val response = authApi.clientLogin(
            loginType = type.value,
            socialAccessToken = socialAccessToken,
        ).unwrapResult()

        sessionRepository.saveTokens(response.accessToken, response.refreshToken)
        sessionRepository.saveLastLoginType(type)
        val status = AuthStatus(
            personalInfoCompleted = response.personalInfoCompleted,
            groupOnboardingCompleted = response.groupOnboardingCompleted,
            mainAccessible = response.mainAccessible,
        )
        sessionRepository.saveStatus(status)
        // 로그인 직후 이 기기 FCM 토큰 서버 등록(앱 재시작 없이도 푸시 수신되도록). fire-and-forget.
        pushTokenSynchronizer.sync()
        return status
    }

    override suspend fun logout() {
        // 토큰 유효할 때(clear 전에) 이 기기 푸시 토큰 비활성 → 로그아웃 후 푸시 안 감.
        runCatchingIgnoringCancellation { pushTokenRepository.unregister() }
        val refreshToken = sessionRepository.getRefreshToken()
        if (refreshToken.isNotBlank()) {
            runCatchingIgnoringCancellation { authApi.logout(TokenLogoutRequest(refreshToken)) }
        }
        sessionRepository.clear()
    }

    override suspend fun withdraw() {
        // 서버 계정 삭제 성공 후에만 로컬 세션 초기화(실패 시 예외 전파 → 화면에서 처리).
        // unregister 를 먼저 하면 withdraw 실패 시 "로그인 상태인데 푸시만 죽는" 상태가 됨 → 삭제 성공 뒤로.
        authApi.withdraw().unwrapResult()
        runCatchingIgnoringCancellation { pushTokenRepository.unregister() }
        sessionRepository.clear()
    }

    /**
     * 실패를 무음 무시하되 [CancellationException] 은 재던짐.
     * runCatching 이 취소까지 삼키면 구조적 동시성이 깨져 상위 취소가 전파 안 됨.
     */
    private inline fun runCatchingIgnoringCancellation(block: () -> Unit) {
        runCatching(block).onFailure { if (it is CancellationException) throw it }
    }
}
