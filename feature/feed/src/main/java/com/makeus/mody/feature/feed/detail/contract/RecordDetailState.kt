package com.makeus.mody.feature.feed.detail.contract

import androidx.compose.ui.text.input.TextFieldValue
import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.feature.CommentFeature
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi

/** 댓글 한 줄 표시 모델. */
data class CommentUi(
    val id: Long,
    val authorName: String,
    val avatarUrl: String?,
    val content: String,
    val isMine: Boolean,
    /** 이모지 하나만 있는 댓글이면 크게 그릴 이모지. null 이면 [segments] 를 말풍선에 그린다. */
    val emoji: CommentEmoji? = null,
    /** 글 + 이모지 토큰을 나눈 본문 조각. 비어 있으면 [content] 를 그대로 그린다. */
    val segments: List<CommentSegment> = emptyList(),
    /** 작성 시각 표시 문구(예: "14:04", 오늘이 아니면 "11/14 14:04"). 시각이 없으면 null. */
    val timeLabel: String? = null,
    /** 서버 응답 전 미리 보여주는 내 이모지(반투명, 꾹 눌러 삭제 불가). */
    val isPending: Boolean = false,
)

/** 전송 중인 내 이모지 미리보기. 보낸 기록을 함께 붙들어, 넘겨도 다른 기록에 안 보이게 한다. */
data class PendingEmoji(val recordId: Long, val comment: CommentUi)

data class RecordDetailState(
    val isLoading: Boolean = true,
    // 좌우로 넘겨보는 기록들
    val records: List<FeedCardUi> = emptyList(),
    val currentIndex: Int = 0,
    // 현재 보고 있는 기록의 댓글
    val comments: List<CommentUi> = emptyList(),
    val isCommentsLoading: Boolean = false,
    /** 입력칸 글과 커서. 이모지를 커서 자리에 끼워 넣어야 해서 문자열이 아니라 [TextFieldValue] 로 둔다. */
    val commentInput: TextFieldValue = TextFieldValue(),
    /**
     * 입력칸 포커스 해제 요청(전송할 때마다 1씩 늘린다). 화면이 바뀐 값을 보고 포커스를 풀고 키보드를 내린다.
     * 불리언이 아니라 카운터인 건, 연달아 보내도 매번 다른 값이라 화면이 놓치지 않게 하려는 것.
     */
    val clearInputFocusRequest: Int = 0,
    val isSending: Boolean = false,
    /** 전송 실패 안내. 이모지는 입력이 남지 않아 알리지 않으면 보낸 줄 안다. */
    val sendError: String? = null,
    val pendingEmojis: List<PendingEmoji> = emptyList(),
    /** 이모지 도배 제한 안내(한 번 띄우고 지운다). */
    val emojiLimitNotice: String? = null,
    // 댓글 삭제: 확인 다이얼로그 대상(null 이면 닫힘) → 삭제 중 → 실패 다이얼로그
    val commentDeleteTargetId: Long? = null,
    val isDeletingComment: Boolean = false,
    val commentDeleteError: String? = null,
    // 댓글 신고: 확인 다이얼로그 대상(null 이면 닫힘) → 접수 중. 완료/실패는 게시물 신고 다이얼로그를 같이 쓴다.
    val commentReportTargetId: Long? = null,
    /** 응원 댓글 노출 여부. 끄면 목록·입력바가 숨고 전송도 막힌다. 출처는 [CommentFeature]. */
    val commentEnabled: Boolean = CommentFeature.ENABLED,
    // 내 memberId(/mypage/me). 로딩 전(null)에는 더보기 메뉴 자체를 숨긴다 — 판별이 뒤집히면
    // 내 글에 "신고"가, 남의 글에 "삭제"가 잠깐 떴다 바뀐다(피드와 같은 정책).
    val myMemberId: Long? = null,
    // 신고 플로우: 확인 다이얼로그 대상 기록(null 이면 닫힘) → 접수 중 → 완료/실패 다이얼로그
    val reportTargetRecordId: Long? = null,
    val isReporting: Boolean = false,
    val showReportComplete: Boolean = false,
    val reportError: String? = null,
    // 삭제 플로우: 확인 다이얼로그 대상 기록(null 이면 닫힘) → 삭제 중 → 실패 다이얼로그.
    // 완료 다이얼로그는 없다 — 슬라이드에서 사라지는(마지막이면 화면이 닫히는) 것이 곧 결과다.
    val deleteTargetRecordId: Long? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
) : UiState {
    /** 전송 가능: 공백 아님 + 전송 중 아님. */
    val canSend: Boolean get() = commentInput.text.isNotBlank() && !isSending

    /** 화면에 그릴 댓글: 받은 댓글 + 지금 기록으로 보내는 중인 이모지. 오래된 → 최신. */
    val visibleComments: List<CommentUi>
        get() {
            val recordId = currentRecord?.id
            return comments + pendingEmojis.filter { it.recordId == recordId }.map { it.comment }
        }

    /** 지금 보고 있는 기록. 탑바 작성자·더보기 메뉴의 대상. */
    val currentRecord: FeedCardUi? get() = records.getOrNull(currentIndex)

    /** 지금 기록이 내 글인지. [myMemberId] 로딩 전이면 null(메뉴 숨김). */
    val isCurrentMine: Boolean?
        get() {
            val me = myMemberId ?: return null
            val record = currentRecord ?: return null
            return record.memberId == me
        }
}
