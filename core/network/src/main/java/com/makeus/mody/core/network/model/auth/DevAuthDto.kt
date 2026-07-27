package com.makeus.mody.core.network.model.auth

import kotlinx.serialization.Serializable

/**
 * 심사용 목 멤버 생성 요청.
 * 개인정보 필드(nickname 등)를 보내면 서버가 personalInfoCompleted=true 로 간주하므로,
 * 온보딩부터 체험시키려면 flag 만 보내고 나머지는 생략(null → 직렬화 제외)해야 한다.
 */
@Serializable
data class CreateMockMemberRequest(
    val personalInfoCompleted: Boolean,
    val groupOnboardingCompleted: Boolean,
    val nickname: String? = null,
    val birthDate: String? = null,
    val targetWeightKg: Double? = null,
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
