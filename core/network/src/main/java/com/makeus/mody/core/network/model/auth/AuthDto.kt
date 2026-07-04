package com.makeus.mody.core.network.model.auth

import kotlinx.serialization.Serializable

/** 소셜 로그인/토큰 재발급 응답의 토큰 + 진행 상태 flag. */
@Serializable
data class SocialLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val personalInfoCompleted: Boolean = false,
    val groupOnboardingCompleted: Boolean = false,
    val mainAccessible: Boolean = false,
)

@Serializable
data class TokenReissueResponse(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class OAuthRedirectUrlResponse(
    val redirectUrl: String,
)

@Serializable
data class TokenReissueRequest(
    val refreshToken: String,
)

@Serializable
data class TokenLogoutRequest(
    val refreshToken: String,
)
