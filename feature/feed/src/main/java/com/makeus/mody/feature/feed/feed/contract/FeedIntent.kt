package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiIntent
import java.time.LocalDate

sealed class FeedIntent : UiIntent {
    /** 화면 복귀(ON_RESUME) → 기록 작성 후 등 캘린더/피드 재조회 */
    data object ScreenResumed : FeedIntent()

    /** 그룹명 셀렉터 탭 → 그룹 선택 시트 열기 */
    data object GroupSelectorClicked : FeedIntent()

    /** 그룹 선택 시트에서 그룹 선택 */
    data class GroupSelected(val groupId: Long) : FeedIntent()

    /** 그룹 선택 시트 닫기 */
    data object GroupSheetDismissed : FeedIntent()

    /** 그룹 선택 시트 "그룹 추가하기" → 참여/생성 선택 다이얼로그 열기 */
    data object AddGroupClicked : FeedIntent()

    /** 추가 다이얼로그 "그룹 참여하기" → 코드 입력 화면 */
    data object JoinGroupClicked : FeedIntent()

    /** 추가 다이얼로그 "그룹 생성하기" → 그룹 생성 화면 */
    data object CreateGroupClicked : FeedIntent()

    /** 추가 다이얼로그 닫기 */
    data object AddGroupDialogDismissed : FeedIntent()

    /** 피드 목록 끝 도달 → 다음 페이지 로드 */
    data object LoadMoreFeeds : FeedIntent()

    /** 알림 아이콘 탭 */
    data object AlarmClicked : FeedIntent()

    /** 주간 스트립 이전/다음 주 */
    data object PrevWeekClicked : FeedIntent()
    data object NextWeekClicked : FeedIntent()

    /** 주간 스트립 날짜 선택 */
    data class DaySelected(val date: LocalDate) : FeedIntent()

    /** 엠티 스테이트 "콕 찌르기 하러 가기" 버튼 탭 */
    data object PokeClicked : FeedIntent()

    /** 피드 카드 탭 → 댓글(chat) 화면 */
    data class FeedCardClicked(val id: Long) : FeedIntent()

    /** 카드 미트볼 메뉴 "신고" 탭 → 신고 확인 다이얼로그 */
    data class ReportClicked(val recordId: Long) : FeedIntent()

    /** 신고 확인 다이얼로그 "신고하기" → 서버 접수 */
    data object ReportConfirmed : FeedIntent()

    /** 신고 확인 다이얼로그 닫기(취소/스크림) */
    data object ReportDialogDismissed : FeedIntent()

    /** 신고 완료 다이얼로그 "확인" */
    data object ReportCompleteConfirmed : FeedIntent()

    /** 신고 실패 다이얼로그 소비 */
    data object ReportErrorShown : FeedIntent()

    /** 카드 미트볼 메뉴 "삭제" 탭 → 삭제 확인 다이얼로그 (내 게시물에만 노출) */
    data class DeleteClicked(val recordId: Long) : FeedIntent()

    /** 삭제 확인 다이얼로그 "삭제하기" → 서버 삭제 */
    data object DeleteConfirmed : FeedIntent()

    /** 삭제 확인 다이얼로그 닫기(취소/스크림) */
    data object DeleteDialogDismissed : FeedIntent()

    /** 삭제 실패 다이얼로그 소비 */
    data object DeleteErrorShown : FeedIntent()

    /** FAB 탭 → 기록 메뉴 확장/축소 */
    data object FabClicked : FeedIntent()

    /** FAB 확장 상태에서 딤 영역 탭 → 축소 */
    data object FabDismissed : FeedIntent()

    /** 운동 기록 작성 */
    data object WriteExerciseClicked : FeedIntent()

    /** 식사 기록 작성 */
    data object WriteMealClicked : FeedIntent()
}
