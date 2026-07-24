package com.makeus.mody.core.network.model.schedule

import kotlinx.serialization.Serializable

/**
 * 식사/운동 알림 스케줄 공용 DTO. 온보딩 프로필 제출과 마이페이지 스케줄 PUT 이 같은 형식을 쓴다.
 * time 은 "HH:mm" (canonical — "HH:mm:ss" 는 스케줄 API 에서 MYPAGE301 로 거부됨).
 */
@Serializable
data class MealScheduleItem(
    /** BREAKFAST / LUNCH / DINNER */
    val mealType: String,
    /** 서버 규칙(MEMBER308): skipped=true 면 null(필드 생략), 먹는 식사만 필수. */
    val time: String? = null,
    val skipped: Boolean = false,
)

/** dayOfWeek: MONDAY..SUNDAY. */
@Serializable
data class ExerciseScheduleItem(
    val dayOfWeek: String,
    val time: String,
)
