package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.PushTokenRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.model.auth.TokenLogoutRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionRepository: SessionRepository,
    private val pushTokenRepository: PushTokenRepository,
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
        return status
    }

    override suspend fun logout() {
        // 토큰 유효할 때(clear 전에) 이 기기 푸시 토큰 비활성 → 로그아웃 후 푸시 안 감.
        runCatching { pushTokenRepository.unregister() }
        val refreshToken = sessionRepository.getRefreshToken()
        if (refreshToken.isNotBlank()) {
            runCatching { authApi.logout(TokenLogoutRequest(refreshToken)) }
        }
        sessionRepository.clear()
    }

    override suspend fun withdraw() {
        runCatching { pushTokenRepository.unregister() }
        // 서버 계정 삭제 성공 후에만 로컬 세션 초기화(실패 시 예외 전파 → 화면에서 처리).
        authApi.withdraw().unwrapResult()
        sessionRepository.clear()
    }
}
