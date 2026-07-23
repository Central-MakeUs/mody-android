package com.makeus.mody.core.data.mapper

import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MealType
import com.makeus.mody.core.domain.model.NotificationSettings
import com.makeus.mody.core.network.model.mypage.ExerciseScheduleItem
import com.makeus.mody.core.network.model.mypage.MealScheduleItem
import com.makeus.mody.core.network.model.mypage.NotificationSettingResponse

private val DAY_OF_WEEK = arrayOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
)

/** "HH:mm:ss" → (hour, minute). 파싱 실패 시 (0,0). */
private fun parseTime(time: String?): Pair<Int, Int> {
    val parts = time?.split(":") ?: return 0 to 0
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h to m
}

// 서버 스케줄 API canonical 형식은 "HH:mm"(초 없음). "HH:mm:ss" 전송 시 MYPAGE301 로 거부됨.
private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

/** 서버 응답 → 도메인 알림 설정. */
fun NotificationSettingResponse.toDomain(): NotificationSettings = NotificationSettings(
    recordReminderEnabled = recordReminderEnabled,
    commentNotificationEnabled = commentNotificationEnabled,
    challengeNotificationEnabled = challengeNotificationEnabled,
    meals = mealSchedules.mapNotNull { it.toDomain() },
    exercises = exerciseSchedules.mapNotNull { it.toDomain() },
)

private fun MealScheduleItem.toDomain(): MealSchedule? {
    val type = runCatching { MealType.valueOf(mealType) }.getOrNull() ?: return null
    val (h, m) = parseTime(time)
    return MealSchedule(type = type, hour = h, minute = m, skipped = skipped)
}

private fun ExerciseScheduleItem.toDomain(): ExerciseSchedule? {
    val day = DAY_OF_WEEK.indexOf(dayOfWeek).takeIf { it >= 0 }?.plus(1) ?: return null
    val (h, m) = parseTime(time)
    return ExerciseSchedule(dayOfWeek = day, hour = h, minute = m)
}

fun MealSchedule.toItem(): MealScheduleItem = MealScheduleItem(
    mealType = type.name,
    // 알림 끔(skipped)이어도 시각은 서버 필수라 채워 보냄.
    time = formatTime(hour, minute),
    skipped = skipped,
)

fun ExerciseSchedule.toItem(): ExerciseScheduleItem = ExerciseScheduleItem(
    dayOfWeek = DAY_OF_WEEK[dayOfWeek - 1],
    time = formatTime(hour, minute),
)
