package com.makeus.mody.core.data.mapper

import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MealType
import com.makeus.mody.core.network.model.schedule.ExerciseScheduleItem
import com.makeus.mody.core.network.model.schedule.MealScheduleItem

/** 식사/운동 스케줄 도메인 ↔ DTO 공용 매핑. 온보딩/알림설정이 함께 쓴다. */

internal val DAY_OF_WEEK = arrayOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
)

/** "HH:mm(:ss)" → (hour, minute). 파싱 실패 시 (0,0). */
internal fun parseTime(time: String?): Pair<Int, Int> {
    val parts = time?.split(":") ?: return 0 to 0
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h to m
}

// 서버 스케줄 API canonical 형식은 "HH:mm"(초 없음). "HH:mm:ss" 전송 시 MYPAGE301 로 거부됨.
internal fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

fun MealScheduleItem.toDomain(): MealSchedule? {
    val type = runCatching { MealType.valueOf(mealType) }.getOrNull() ?: return null
    val (h, m) = parseTime(time)
    return MealSchedule(type = type, hour = h, minute = m, skipped = skipped)
}

fun ExerciseScheduleItem.toDomain(): ExerciseSchedule? {
    val day = DAY_OF_WEEK.indexOf(dayOfWeek).takeIf { it >= 0 }?.plus(1) ?: return null
    val (h, m) = parseTime(time)
    return ExerciseSchedule(dayOfWeek = day, hour = h, minute = m)
}

fun MealSchedule.toItem(): MealScheduleItem = MealScheduleItem(
    mealType = type.name,
    // 서버 규칙(MEMBER308): skipped 면 time 을 비워야 하고, 먹는 식사만 time 필수.
    time = if (skipped) null else formatTime(hour, minute),
    skipped = skipped,
)

fun ExerciseSchedule.toItem(): ExerciseScheduleItem = ExerciseScheduleItem(
    dayOfWeek = DAY_OF_WEEK[dayOfWeek - 1],
    time = formatTime(hour, minute),
)
