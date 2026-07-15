package com.makeus.mody.feature.feed.detail.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordDetailIntent : UiIntent {
    /** 뒤로가기 */
    data object BackClicked : RecordDetailIntent()

    /** 페이저가 index 로 넘어감 → 해당 기록 댓글 로드 */
    data class PageChanged(val index: Int) : RecordDetailIntent()

    /** 댓글 입력 변경 */
    data class CommentInputChanged(val text: String) : RecordDetailIntent()

    /** 댓글 전송 */
    data object SendCommentClicked : RecordDetailIntent()
}
