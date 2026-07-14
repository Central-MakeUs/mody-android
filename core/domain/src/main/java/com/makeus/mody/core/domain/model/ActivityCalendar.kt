package com.makeus.mody.core.domain.model

import java.time.LocalDate

/** 주간(일~토) 기록 캘린더. 그룹원 중 한 명이라도 기록한 날 hasRecord = true. */
data class ActivityCalendar(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val days: List<ActivityDay>,
)

data class ActivityDay(
    val date: LocalDate,
    val hasRecord: Boolean,
)
