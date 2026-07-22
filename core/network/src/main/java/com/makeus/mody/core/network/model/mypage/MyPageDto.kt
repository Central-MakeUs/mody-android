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

/** POST /api/v1/mypage/weights — 체중 기록 생성. */
@Serializable
data class MyPageWeightCreateRequest(
    /** 기록 날짜(ISO, yyyy-MM-dd). */
    val recordedOn: String,
    /** 체중(kg). 서버 허용 20.0~300.0. */
    val weightKg: Double,
)

/** POST /api/v1/mypage/weights 응답. */
@Serializable
data class MyPageWeightCreateResponse(
    val weightRecordId: Long? = null,
    val recordedOn: String? = null,
    val weightKg: Double? = null,
    val changeFromPreviousKg: Double? = null,
)

/** GET /api/v1/mypage/profile — 프로필 상세(로그인 수단·이름·생년월일). */
@Serializable
data class MyPageProfileResponse(
    val loginType: String = "",
    val name: String = "",
    val birthDate: String? = null,
)

/** PATCH /api/v1/mypage/profile — 이름/생년월일 수정. */
@Serializable
data class MyPageProfileUpdateRequest(
    val nickname: String,
    val birthDate: String?,
)
