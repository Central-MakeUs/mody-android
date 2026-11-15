package com.makeus.mody.core.model

import java.time.Instant

/** 기록 댓글 한 건. isMine = 내가 쓴 댓글(삭제 등 UI 분기용). */
data class Comment(
    val commentId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val content: String,
    val isMine: Boolean,
    /** 이모지 댓글이면 이모지 아이디, 글 댓글은 null. 그리는 쪽이 아이디 ↔ 이미지를 매핑한다. */
    val emojiId: String? = null,
    /** 작성 시각. 서버가 안 주거나 형식이 틀리면 null(시각 표시만 빠진다). */
    val createdAt: Instant? = null,
)

/** 댓글 커서 페이지네이션 결과. */
data class CommentPage(
    val comments: List<Comment>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
