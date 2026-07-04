package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.SocialLoginType

/**
 * 소셜 SDK(Kakao/Google) 로그인으로 소셜 accessToken 을 획득하는 추상화.
 * 실제 구현은 각 SDK 연동(feature:auth). 반환한 토큰을 [AuthRepository.loginWithSocial] 에 넘긴다.
 */
interface SocialLoginProvider {
    suspend fun getAccessToken(type: SocialLoginType): String
}
