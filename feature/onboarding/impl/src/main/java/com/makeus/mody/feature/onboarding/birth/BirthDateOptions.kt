package com.makeus.mody.feature.onboarding.birth

import java.time.LocalDate
import java.time.YearMonth

/**
 * 생년월일 피커에 들어가는 연/월/일 후보와 보정 로직.
 * Compose 와 무관한 순수 계산이라 화면에서 분리해 단위 테스트 가능하게 둔다.
 */
object BirthDateOptions {

    /** 가입 가능 최소 한국나이. 14세 이상만 사용 가능. */
    const val MIN_KOREAN_AGE = 14

    /** 선택 가능한 가장 이른 출생연도. */
    const val MIN_YEAR = 1950

    val months: List<Int> = (1..12).toList()

    /**
     * 선택 가능한 출생연도 상한 = 기준일 연도 - (최소나이 - 1).
     * 한국나이는 (기준연도 - 출생연도 + 1) 이므로 14세 이상 → 출생연도 ≤ 기준연도 - 13.
     */
    fun maxYear(today: LocalDate = LocalDate.now()): Int =
        today.year - (MIN_KOREAN_AGE - 1)

    fun years(today: LocalDate = LocalDate.now()): List<Int> =
        (MIN_YEAR..maxYear(today)).toList()

    /** 해당 연/월의 실제 일수(1..28/29/30/31). */
    fun days(year: Int, month: Int): List<Int> =
        (1..YearMonth.of(year, month).lengthOfMonth()).toList()

    /** 연/월 변경 시 존재하지 않는 날(예: 2월 31일)로 새지 않도록 day 를 보정. */
    fun clampDay(year: Int, month: Int, day: Int): Int =
        day.coerceIn(1, YearMonth.of(year, month).lengthOfMonth())
}
