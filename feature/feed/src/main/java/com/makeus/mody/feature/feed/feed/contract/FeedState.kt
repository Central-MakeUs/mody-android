package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.CropRegion
import java.time.LocalDate

/** 피드 카드 표시 모델. TODO(feed): API 연동 시 도메인 모델 매핑으로 교체. */
data class FeedCardUi(
    val id: Long,
    /** 작성자 memberId. 내 게시물(신고 미노출) 판별용. */
    val memberId: Long = 0,
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
    /** 원본 이미지에서 표시할 크롭 영역(정규화). null 이면 원본 전체. */
    val cropRegion: CropRegion? = null,
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
    // 챌린지 기능 노출(Remote Config is_phase_one_flag). Phase 1 에선 콕 찌르기 등 숨김.
    val phaseTwoFeaturesEnabled: Boolean = false,
    /**
     * 응원 댓글 노출(Remote Config comment_flag). 꺼져 있으면 카드의 상세 진입 화살표를
     * 숨기고 탭도 막는다 — 상세 화면의 고유 콘텐츠가 댓글뿐이라(사진은 이 목록에 이미
     * 다 있다) 댓글을 가리면 들어가도 볼 게 없다.
     */
    val commentEnabled: Boolean = false,
    // 상단바 알림 아이콘 뱃지(안 읽은 알림 존재).
    val hasUnreadNotification: Boolean = false,
    // 내 memberId(/mypage/me). 로딩 전(null)에는 메뉴 자체를 숨긴다 — 판별이 뒤집히면
    // 내 글에 "신고"가, 남의 글에 "삭제"가 잠깐 떴다 바뀐다.
    val myMemberId: Long? = null,
    // 신고 플로우: 확인 다이얼로그 대상 기록(null 이면 닫힘) → 접수 중 → 완료/실패 다이얼로그
    val reportTargetRecordId: Long? = null,
    val isReporting: Boolean = false,
    val showReportComplete: Boolean = false,
    val reportError: String? = null,
    // 삭제 플로우: 확인 다이얼로그 대상 기록(null 이면 닫힘) → 삭제 중 → 실패 다이얼로그.
    // 신고와 달리 완료 다이얼로그가 없다 — 카드가 목록에서 사라지는 것이 곧 결과다.
    val deleteTargetRecordId: Long? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
) : UiState {
    val isEmpty: Boolean get() = feeds.isEmpty()
}
