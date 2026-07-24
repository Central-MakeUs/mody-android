package com.makeus.mody.core.domain.model

/**
 * 알림 설정. 토글 3개 + 식사/운동 스케줄.
 * 식사/운동 모델은 온보딩과 동일([MealSchedule]/[ExerciseSchedule]) 재사용.
 */
data class NotificationSettings(
    /** 식사 및 운동 알림(끄면 식사/운동 스케줄 알림 전체 중지). */
    val recordReminderEnabled: Boolean,
    /** 코멘트 알림. */
    val commentNotificationEnabled: Boolean,
    /** 챌린지 알림. */
    val challengeNotificationEnabled: Boolean,
    val meals: List<MealSchedule>,
    val exercises: List<ExerciseSchedule>,
)
