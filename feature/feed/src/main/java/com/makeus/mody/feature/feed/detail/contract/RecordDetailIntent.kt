package com.makeus.mody.feature.feed.detail.contract

import androidx.compose.ui.text.input.TextFieldValue
import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordDetailIntent : UiIntent {
    /** 뒤로가기 */
    data object BackClicked : RecordDetailIntent()

    /** 페이저가 index 로 넘어감 → currentIndex 갱신 (댓글은 게시물 단위 통일이라 재조회 안 함) */
    data class PageChanged(val index: Int) : RecordDetailIntent()

    /** 댓글 입력 변경 */
    data class CommentInputChanged(val value: TextFieldValue) : RecordDetailIntent()

    /** 댓글 전송 */
    data object SendCommentClicked : RecordDetailIntent()

    /** 이모지 탭 → 입력칸이 비었으면 바로 전송, 글을 쓰는 중이면 커서 자리에 넣기 */
    data class EmojiClicked(val emoji: CommentEmoji) : RecordDetailIntent()

    /** 전송 실패 다이얼로그 닫힘 */
    data object SendErrorShown : RecordDetailIntent()

    /** 이모지 도배 제한 안내를 띄웠음 */
    data object EmojiLimitNoticeShown : RecordDetailIntent()

    /** 내 댓글 꾹 누름 → 뜬 "삭제" 탭 */
    data class CommentDeleteClicked(val commentId: Long) : RecordDetailIntent()

    /** 댓글 삭제 확인 "삭제하기" */
    data object CommentDeleteConfirmed : RecordDetailIntent()

    /** 댓글 삭제 확인 다이얼로그 닫기 */
    data object CommentDeleteDialogDismissed : RecordDetailIntent()

    /** 댓글 삭제 실패 다이얼로그 닫힘 */
    data object CommentDeleteErrorShown : RecordDetailIntent()

    /** 더보기 → 신고(남의 글). 대상은 지금 보고 있는 기록. */
    data object ReportClicked : RecordDetailIntent()

    /** 신고 확인 다이얼로그 "신고하기" */
    data object ReportConfirmed : RecordDetailIntent()

    /** 신고 확인 다이얼로그 닫기 */
    data object ReportDialogDismissed : RecordDetailIntent()

    /** 신고 완료 다이얼로그 확인 */
    data object ReportCompleteConfirmed : RecordDetailIntent()

    /** 신고 실패 다이얼로그 닫힘 */
    data object ReportErrorShown : RecordDetailIntent()

    /** 더보기 → 삭제(내 글). 대상은 지금 보고 있는 기록. */
    data object DeleteClicked : RecordDetailIntent()

    /** 삭제 확인 다이얼로그 "삭제하기" */
    data object DeleteConfirmed : RecordDetailIntent()

    /** 삭제 확인 다이얼로그 닫기 */
    data object DeleteDialogDismissed : RecordDetailIntent()

    /** 삭제 실패 다이얼로그 닫힘 */
    data object DeleteErrorShown : RecordDetailIntent()
}
