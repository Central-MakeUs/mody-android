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
    // 오늘 이후 미래 날짜 — 선택 불가(불러올 기록 없음), 흐리게 표시.
    val isFuture: Boolean = false,
)

/** 그룹 선택 시트 한 줄. */
data class GroupUi(
    val id: Long,
    val name: String,
    val code: String,
    val isCurrent: Boolean,
)

data class FeedState(
    // 상단 그룹 셀렉터 (예: "아자아자")
    val groupName: String = "",
    // 주차 라벨 (예: "7월 2주차")
    val weekLabel: String = "",
    // 일요일 시작 7일
    val weekDays: List<WeekDayUi> = emptyList(),
    // 다음 주 이동 가능 여부(이번 주면 미래라 false → 다음 주 버튼 비활성)
    val canGoNextWeek: Boolean = false,
    val feeds: List<FeedCardUi> = emptyList(),
    val isLoading: Boolean = false,
    // 커서 페이지네이션(무한 스크롤)
    val hasMoreFeeds: Boolean = false,
    val isLoadingMore: Boolean = false,
    // 피드 작성 FAB 확장
    val isFabExpanded: Boolean = false,
    // 그룹 선택 시트
    val groups: List<GroupUi> = emptyList(),
    val isGroupSheetVisible: Boolean = false,
    // 그룹 추가 방식(참여/생성) 선택 다이얼로그
    val isAddGroupDialogVisible: Boolean = false,
) : UiState {
    val isEmpty: Boolean get() = feeds.isEmpty()
}
