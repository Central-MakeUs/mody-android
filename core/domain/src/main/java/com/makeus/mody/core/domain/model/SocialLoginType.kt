package com.makeus.mody.core.domain.model

/** 소셜 로그인 종류. [value] 는 서버 loginType 경로 값. */
enum class SocialLoginType(val value: String) {
    KAKAO("kakao"),
    GOOGLE("google"),
}
