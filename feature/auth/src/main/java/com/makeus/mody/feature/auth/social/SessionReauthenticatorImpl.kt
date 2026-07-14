package com.makeus.mody.feature.auth.social

import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.SessionReauthenticator
import com.makeus.mody.core.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * refresh 토큰 만료 시 소셜 SDK 세션으로 무음 재로그인.
 * 마지막 로그인 타입의 SDK에서 UI 없이 소셜 토큰을 얻어 서버 로그인을 다시 태운다.
 * 성공하면 [AuthRepository.loginWithSocial] 이 새 JWT/상태를 저장한다.
 */
@Singleton
class SessionReauthenticatorImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val kakaoLoginProvider: KakaoLoginProvider,
) : SessionReauthenticator {

    override suspend fun reauthenticate(): Boolean {
        val type = sessionRepository.getLastLoginType() ?: return false
        return when (type) {
            SocialLoginType.KAKAO -> {
                val socialToken = kakaoLoginProvider.getAccessTokenSilently() ?: return false
                runCatching { authRepository.loginWithSocial(type, socialToken) }.isSuccess
            }
            // TODO(auth): 구글 무음 재로그인(Credential Manager). 지금은 로그인 화면으로 유도.
            SocialLoginType.GOOGLE -> false
        }
    }
}
