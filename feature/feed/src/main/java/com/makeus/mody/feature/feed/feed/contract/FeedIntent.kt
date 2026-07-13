package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class FeedIntent : UiIntent {
    /** 날짜 셀렉터 탭 → 캘린더 바텀시트 열기 */
    data object DateClicked : FeedIntent()

    /** 캘린더 닫기(시트 dismiss / X) */
    data object CalendarDismissed : FeedIntent()

    /** 캘린더 이전/다음 달 */
    data object CalendarPrevMonth : FeedIntent()
    data object CalendarNextMonth : FeedIntent()

    /** 캘린더 날짜 선택 */
    data class CalendarDaySelected(val day: Int) : FeedIntent()

    /** 캘린더 확인 버튼 */
    data object CalendarConfirmClicked : FeedIntent()

    /** 그룹 멤버 아이콘 탭 */
    data object MembersClicked : FeedIntent()

    /** 알림 아이콘 탭 */
    data object AlarmClicked : FeedIntent()

    /** 엠티 스테이트 "콕 찌르기 하러 가기" 버튼 탭 */
    data object PokeClicked : FeedIntent()

    /** 피드 카드 탭 → 댓글(chat) 화면 */
    data class FeedCardClicked(val id: Long) : FeedIntent()

    /** FAB 탭 → 기록 메뉴 확장/축소 (Feed4) */
    data object FabClicked : FeedIntent()

    /** FAB 확장 상태에서 딤 영역 탭 → 축소 */
    data object FabDismissed : FeedIntent()

    /** 운동 기록 작성 */
    data object WriteExerciseClicked : FeedIntent()

    /** 식사 기록 작성 */
    data object WriteMealClicked : FeedIntent()
}
