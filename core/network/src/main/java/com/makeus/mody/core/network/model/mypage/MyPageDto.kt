package com.makeus.mody.core.network.model.mypage

import com.makeus.mody.core.network.model.schedule.ExerciseScheduleItem
import com.makeus.mody.core.network.model.schedule.MealScheduleItem
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

/** PATCH /api/v1/mypage/profile — 이름/생년월일/프로필 이미지 수정. */
@Serializable
data class MyPageProfileUpdateRequest(
    val nickname: String,
    val birthDate: String?,
    /**
     * 프로필 이미지 참조 키(업로드 후 발급받은 imageKey).
     *  - null: 이미지 변경 없음(필드 생략 → encodeDefaults=false).
     *  - "": 기본 이미지로 리셋(서버가 빈 값을 기본 아바타로 처리).
     *  - 그 외: 업로드한 imageKey 로 설정.
     */
    val imageKey: String? = null,
)

/** PATCH /api/v1/mypage/profile 응답 — 수정 결과(반영된 프로필 이미지 URL 포함). */
@Serializable
data class MyPageProfileUpdateResponse(
    val nickname: String = "",
    val birthDate: String? = null,
    val profileImageUrl: String? = null,
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

/**
 * PATCH /api/v1/mypage/notification-settings — 알림 토글 수정.
 * 서버가 전체 교체 semantics(보낸 필드만 적용, 안 보낸 필드는 false 리셋)라
 * 세 토글을 항상 모두 채워 보내야 한다. null 허용은 직렬화 형식일 뿐 "미변경" 이 아님.
 */
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
