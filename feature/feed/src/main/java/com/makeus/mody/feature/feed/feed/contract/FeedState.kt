package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiState
import java.time.LocalDate

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
    val avatarUrl: String? = null,
    val imageUrl: String? = null,
)

/** 주간 스트립 한 칸 (일~토 7개). */
data class WeekDayUi(
    val date: LocalDate,
    val weekdayLabel: String, // "일" ~ "토"
    val isSelected: Boolean,
    val hasFeed: Boolean,
)

data class FeedState(
    // 상단 그룹 셀렉터 (예: "아자아자")
    val groupName: String = "",
    // 주차 라벨 (예: "7월 2주차")
    val weekLabel: String = "",
    // 일요일 시작 7일
    val weekDays: List<WeekDayUi> = emptyList(),
    val feeds: List<FeedCardUi> = emptyList(),
    val isLoading: Boolean = false,
    // 피드 작성 FAB 확장
    val isFabExpanded: Boolean = false,
) : UiState {
    val isEmpty: Boolean get() = feeds.isEmpty()
}
