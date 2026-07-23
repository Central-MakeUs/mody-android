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

/** 식사 알림 스케줄. time: "HH:mm:ss"(LocalTime), skipped=true면 해당 끼니 알림 끔. */
@Serializable
data class MealScheduleItem(
    /** BREAKFAST / LUNCH / DINNER */
    val mealType: String,
    val time: String? = null,
    val skipped: Boolean = false,
)

/** 운동 알림 스케줄. dayOfWeek: MONDAY..SUNDAY, time: "HH:mm:ss". */
@Serializable
data class ExerciseScheduleItem(
    val dayOfWeek: String,
    val time: String,
)

/** GET /api/v1/mypage/notification-settings — 알림 설정(토글 3개 + 식사/운동 스케줄). */
@Serializable
data class NotificationSettingResponse(
    /** 식사 및 운동 알림. */
    val recordReminderEnabled: Boolean = false,
    val commentNotificationEnabled: Boolean = false,
    val challengeNotificationEnabled: Boolean = false,
    val mealSchedules: List<MealScheduleItem> = emptyList(),
    val exerciseSchedules: List<ExerciseScheduleItem> = emptyList(),
)

/** PATCH /api/v1/mypage/notification-settings — 토글만 수정(null이면 미변경). */
@Serializable
data class NotificationSettingRequest(
    val recordReminderEnabled: Boolean? = null,
    val commentNotificationEnabled: Boolean? = null,
    val challengeNotificationEnabled: Boolean? = null,
)

/** PUT /api/v1/mypage/schedules — 식사(3)/운동 스케줄 갱신. */
@Serializable
data class ScheduleRequest(
    val mealSchedules: List<MealScheduleItem>,
    val exerciseSchedules: List<ExerciseScheduleItem>,
)

/** PUT /api/v1/mypage/schedules 응답(식사/운동 스케줄). */
@Serializable
data class ScheduleResponse(
    val mealSchedules: List<MealScheduleItem> = emptyList(),
    val exerciseSchedules: List<ExerciseScheduleItem> = emptyList(),
)
