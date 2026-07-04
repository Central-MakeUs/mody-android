package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.interceptor.TokenManager
import com.makeus.mody.core.network.model.auth.TokenLogoutRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionRepository: SessionRepository,
    private val tokenManager: TokenManager,
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
        val status = AuthStatus(
            personalInfoCompleted = response.personalInfoCompleted,
            groupOnboardingCompleted = response.groupOnboardingCompleted,
            mainAccessible = response.mainAccessible,
        )
        sessionRepository.saveStatus(status)
        return status
    }

    override suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNotBlank()) {
            runCatching { authApi.logout(TokenLogoutRequest(refreshToken)) }
        }
        sessionRepository.clear()
    }
}
