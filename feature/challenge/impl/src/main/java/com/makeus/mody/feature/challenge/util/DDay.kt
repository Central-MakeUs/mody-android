package com.makeus.mody.feature.challenge.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/**
 * 마감 요일(서버 enum MONDAY~SUNDAY) → 오늘 기준 D-N.
 * 파싱 실패 시 빈 문자열 대신 받은 값을 그대로 보여준다(원인을 화면에서 알 수 있게).
 */
internal fun dDayLabel(deadlineDayOfWeek: String): String {
    val target = runCatching {
        DayOfWeek.valueOf(deadlineDayOfWeek.uppercase(Locale.US))
    }.getOrNull() ?: return deadlineDayOfWeek
    val today = LocalDate.now().dayOfWeek
    val days = (target.value - today.value + 7) % 7
    return "D-$days"
}
