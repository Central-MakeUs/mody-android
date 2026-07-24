package com.makeus.mody.core.data.mapper

import com.makeus.mody.core.domain.model.NotificationSettings
import com.makeus.mody.core.network.model.mypage.NotificationSettingResponse

/** 서버 응답 → 도메인 알림 설정. 스케줄 매핑은 [ScheduleMapper] 공용 사용. */
fun NotificationSettingResponse.toDomain(): NotificationSettings = NotificationSettings(
    recordReminderEnabled = recordReminderEnabled,
    commentNotificationEnabled = commentNotificationEnabled,
    challengeNotificationEnabled = challengeNotificationEnabled,
    meals = mealSchedules.mapNotNull { it.toDomain() },
    exercises = exerciseSchedules.mapNotNull { it.toDomain() },
)
