package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiState

/** 피드 카드 표시 모델. TODO(feed): API 연동 시 도메인 모델 매핑으로 교체. */
data class FeedCardUi(
    val id: Long,
    val authorName: String,
    val dayCount: Int,
    // 식사: "식사 시간"/"13:00", "메뉴"/"계란 3알, 사과 2조각"
    // 운동: "운동 시간"/"45분", "운동종류"/"런닝"
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val imageUrl: String? = null,
)

/** 캘린더 한 칸. day = 1~31, inMonth=false 면 다음/이전 달 날짜(회색). */
data class CalendarDayUi(
    val day: Int,
    val inMonth: Boolean,
    val isToday: Boolean,
    val hasFeed: Boolean,
)

data class FeedState(
    // 상단 날짜 표기 (예: "7월 18일")
    val dateLabel: String = "",
    val feeds: List<FeedCardUi> = emptyList(),
    val isLoading: Boolean = false,
    // 캘린더 바텀시트 (Feed3)
    val isCalendarVisible: Boolean = false,
    val calendarTitle: String = "", // 예: "2026.07"
    val calendarDays: List<CalendarDayUi> = emptyList(), // 일요일 시작, 7의 배수
    // 피드 작성 FAB 확장 (Feed4)
    val isFabExpanded: Boolean = false,
) : UiState {
    val isEmpty: Boolean get() = feeds.isEmpty()
}
