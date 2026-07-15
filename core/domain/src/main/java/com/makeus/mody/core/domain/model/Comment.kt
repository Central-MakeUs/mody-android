package com.makeus.mody.core.domain.model

/** 기록 댓글 한 건. isMine = 내가 쓴 댓글(삭제 등 UI 분기용). */
data class Comment(
    val commentId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val content: String,
    val isMine: Boolean,
)

/** 댓글 커서 페이지네이션 결과. */
data class CommentPage(
    val comments: List<Comment>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
