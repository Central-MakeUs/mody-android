package com.makeus.mody.core.network.model.mypage

import kotlinx.serialization.Serializable

/** GET /api/v1/mypage/me — 마이페이지 상단 프로필. */
@Serializable
data class MyPageMeResponse(
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val daysTogether: Int = 0,
    val personalInfoCompleted: Boolean = false,
    val groupOnboardingCompleted: Boolean = false,
    val mainAccessible: Boolean = false,
)

/** GET /api/v1/mypage/weights — 체중 요약(이전/현재/목표). 미기록 시 null. */
@Serializable
data class MyPageWeightsResponse(
    val startWeightKg: Double? = null,
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
)
