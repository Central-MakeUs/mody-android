package com.makeus.mody.core.network.model.auth

import kotlinx.serialization.Serializable

/** 심사용 목 멤버 생성 요청. personalInfoCompleted=true 면 개인정보 온보딩 건너뜀. */
@Serializable
data class CreateMockMemberRequest(
    val nickname: String,
    val birthDate: String,
    val targetWeightKg: Double,
    val personalInfoCompleted: Boolean,
    val groupOnboardingCompleted: Boolean,
)

@Serializable
data class MockMemberResponse(
    val memberId: Long,
    val nickname: String? = null,
    val personalInfoCompleted: Boolean = false,
    val groupOnboardingCompleted: Boolean = false,
)

@Serializable
data class IssueTokenRequest(
    val memberId: Long,
)

/** dev 토큰 발급 응답. 소셜 로그인 응답과 동일한 진행 상태 flag 포함. */
@Serializable
data class DevTokenResponse(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val personalInfoCompleted: Boolean = false,
    val groupOnboardingCompleted: Boolean = false,
    val mainAccessible: Boolean = false,
)
