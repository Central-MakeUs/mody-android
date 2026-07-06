package com.makeus.mody.feature.auth.social

import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.SocialLoginProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 타입에 맞는 SDK 구현체를 선택하는 facade.
 * Provider별 SDK 세부사항은 각각의 구현체가 담당한다.
 */
@Singleton
class SocialLoginProviderImpl @Inject constructor(
    private val kakaoLoginProvider: KakaoLoginProvider,
    private val googleLoginProvider: GoogleLoginProvider,
) : SocialLoginProvider {

    override suspend fun getAccessToken(type: SocialLoginType): String = when (type) {
        SocialLoginType.KAKAO -> kakaoLoginProvider.login()
        SocialLoginType.GOOGLE -> googleLoginProvider.login()
    }
}
