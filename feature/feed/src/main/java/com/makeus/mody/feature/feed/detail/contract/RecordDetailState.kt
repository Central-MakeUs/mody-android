package com.makeus.mody.feature.feed.detail.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi

/** 댓글 한 줄 표시 모델. */
data class CommentUi(
    val id: Long,
    val authorName: String,
    val avatarUrl: String?,
    val content: String,
    val isMine: Boolean,
)

data class RecordDetailState(
    val isLoading: Boolean = true,
    // 좌우로 넘겨보는 기록들
    val records: List<FeedCardUi> = emptyList(),
    val currentIndex: Int = 0,
    // 현재 보고 있는 기록의 댓글
    val comments: List<CommentUi> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val commentInput: String = "",
    val isSending: Boolean = false,
) : UiState {
    /** 전송 가능: 공백 아님 + 전송 중 아님. */
    val canSend: Boolean get() = commentInput.isNotBlank() && !isSending
}
