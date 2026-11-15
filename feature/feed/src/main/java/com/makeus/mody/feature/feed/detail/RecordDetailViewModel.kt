package com.makeus.mody.feature.feed.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.model.Comment
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.domain.repository.RecordRepository
import com.makeus.mody.core.navigation.FeedGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.feed.detail.contract.CommentEmoji
import com.makeus.mody.feature.feed.detail.contract.CommentUi
import com.makeus.mody.feature.feed.detail.contract.RecordDetailIntent
import com.makeus.mody.feature.feed.detail.contract.RecordDetailState
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi
import com.makeus.mody.feature.feed.feed.contract.toFeedCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val recordRepository: RecordRepository,
    private val myPageRepository: MyPageRepository,
    private val navigationHelper: NavigationHelper,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<RecordDetailState, RecordDetailIntent>(RecordDetailState()) {

    private val route = savedStateHandle.toRoute<FeedGraph.RecordDetailRoute>()
    private val groupId = route.groupId
    private val date = LocalDate.parse(route.date)

    /** 진행 중인 댓글 조회. 페이지를 빠르게 넘기면 이전 조회를 취소한다. */
    private var commentsJob: Job? = null

    init {
        loadDetail()
        loadMyMemberId()
        // 댓글 노출은 원격이 아니라 RecordDetailState.commentEnabled 기본값으로 끈다.
    }

    override suspend fun processIntent(intent: RecordDetailIntent) {
        when (intent) {
            is RecordDetailIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
            is RecordDetailIntent.PageChanged -> onPageChanged(intent.index)
            is RecordDetailIntent.CommentInputChanged ->
                setState { copy(commentInput = intent.text) }
            // UI 는 숨겨져 있지만 만약을 위해 전송 자체도 차단.
            is RecordDetailIntent.SendCommentClicked ->
                if (currentState.commentEnabled) sendComment()
            is RecordDetailIntent.EmojiClicked ->
                if (currentState.commentEnabled) sendEmoji(intent.emoji)
            is RecordDetailIntent.SendErrorShown -> setState { copy(sendError = null) }
            is RecordDetailIntent.CommentDeleteClicked ->
                setState { copy(commentDeleteTargetId = intent.commentId) }
            is RecordDetailIntent.CommentDeleteDialogDismissed -> setState { copy(commentDeleteTargetId = null) }
            is RecordDetailIntent.CommentDeleteConfirmed -> deleteComment()
            is RecordDetailIntent.CommentDeleteErrorShown -> setState { copy(commentDeleteError = null) }

            // 대상 id 는 누른 순간에 붙든다 — 다이얼로그가 떠 있는 동안 페이지가 바뀌어도 그 기록이다.
            is RecordDetailIntent.ReportClicked ->
                setState { copy(reportTargetRecordId = currentRecord?.id) }
            is RecordDetailIntent.ReportDialogDismissed -> setState { copy(reportTargetRecordId = null) }
            is RecordDetailIntent.ReportConfirmed -> reportRecord()
            is RecordDetailIntent.ReportCompleteConfirmed -> setState { copy(showReportComplete = false) }
            is RecordDetailIntent.ReportErrorShown -> setState { copy(reportError = null) }
            is RecordDetailIntent.DeleteClicked ->
                setState { copy(deleteTargetRecordId = currentRecord?.id) }
            is RecordDetailIntent.DeleteDialogDismissed -> setState { copy(deleteTargetRecordId = null) }
            is RecordDetailIntent.DeleteConfirmed -> deleteRecord()
            is RecordDetailIntent.DeleteErrorShown -> setState { copy(deleteError = null) }
        }
    }

    /** 내 memberId 조회 — 더보기 메뉴의 신고/삭제 판별값. 실패 시 null 유지(메뉴 미노출). */
    private fun loadMyMemberId() = viewModelScope.launch {
        try {
            val memberId = myPageRepository.getProfile().memberId
            setState { copy(myMemberId = memberId) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 메뉴만 숨겨진다. 상세 보기 자체엔 지장 없음.
        }
    }

    /** 신고 확인 "신고하기" → 서버 접수 후 완료/실패 다이얼로그. 피드 카드 신고와 같은 API·문구. */
    private fun reportRecord() = viewModelScope.launch {
        val recordId = currentState.reportTargetRecordId ?: return@launch
        if (currentState.isReporting) return@launch
        // 다이얼로그는 바로 닫고 오버레이로 대기를 보여준다. 대상 id 는 위에서 붙들었다.
        setState { copy(isReporting = true, reportTargetRecordId = null) }
        try {
            feedRepository.reportRecord(groupId, recordId)
            setState { copy(isReporting = false, showReportComplete = true) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            setState { copy(isReporting = false, reportError = "신고 접수에 실패했어요. 다시 시도해주세요.") }
        }
    }

    /**
     * 삭제 확인 "삭제하기" → 서버 삭제 후 슬라이드에서 제거.
     *
     * 마지막 한 장이었으면 볼 게 없으니 화면을 닫는다. 남아 있으면 같은 자리(없으면 앞)의 기록으로
     * 넘어가고 그 기록의 댓글을 다시 받는다 — 지운 기록의 댓글이 남아 보이면 안 된다.
     * 피드 목록은 돌아갈 때 화면 재개(ScreenResumed)에서 다시 받는다.
     */
    private fun deleteRecord() = viewModelScope.launch {
        val recordId = currentState.deleteTargetRecordId ?: return@launch
        if (currentState.isDeleting) return@launch
        setState { copy(isDeleting = true, deleteTargetRecordId = null) }
        try {
            recordRepository.deleteRecord(recordId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            setState { copy(isDeleting = false, deleteError = "게시물 삭제에 실패했어요. 다시 시도해주세요.") }
            return@launch
        }
        val deletedIndex = currentState.records.indexOfFirst { it.id == recordId }
        val remaining = currentState.records.filterNot { it.id == recordId }
        if (remaining.isEmpty()) {
            setState { copy(isDeleting = false, records = remaining) }
            navigationHelper.navigate(NavigationEvent.Up)
            return@launch
        }
        val nextIndex = (if (deletedIndex >= 0) deletedIndex else currentState.currentIndex)
            .coerceAtMost(remaining.lastIndex)
        setState {
            copy(isDeleting = false, records = remaining, currentIndex = nextIndex, comments = emptyList())
        }
        loadComments(remaining[nextIndex].id)
    }

    /**
     * 상세 슬라이드 로드.
     * 서버의 records/{recordId} 상세는 "탭한 기록 + 그보다 최신"만 주므로(오래된 쪽 누락),
     * 그날 그룹 전체 기록(getRecords)을 모아 슬라이드를 구성하고 탭한 기록 위치를 currentIndex 로.
     */
    private fun loadDetail() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        runCatching { loadAllRecords() }
            .onSuccess { all ->
                val index = all.indexOfFirst { it.id == route.recordId }.coerceAtLeast(0)
                setState {
                    copy(
                        isLoading = false,
                        records = all,
                        currentIndex = index,
                    )
                }
                recordIdAt(index)?.let(::loadComments)
            }
            .onFailure {
                // TODO(feed): 에러 노출 정책. 지금은 빈 상태.
                setState { copy(isLoading = false) }
            }
    }

    /** 그날 그룹 전체 기록을 커서 끝까지 모아 슬라이드용 리스트로 변환. */
    private suspend fun loadAllRecords(): List<FeedCardUi> {
        val result = mutableListOf<FeedCardUi>()
        var cursor: Long? = null
        var hasNext = true
        var guard = 0
        while (hasNext && guard < MAX_DETAIL_PAGES) {
            guard++
            val page = feedRepository.getRecords(groupId, date, cursor = cursor)
            result += page.records.map { it.toFeedCardUi() }
            cursor = page.nextCursor
            hasNext = page.hasNext && cursor != null
        }
        return result.distinctBy { it.id }
    }

    /**
     * 페이지 스와이프 = 다른 기록(작성자도 바뀐다). 댓글은 기록 단위라 그 기록 것을 다시 받는다.
     * 이전 기록의 댓글이 남아 보이면 그 위에 쓴 응원이 다른 사람 기록에 달린다.
     */
    private fun onPageChanged(index: Int) {
        if (index == currentState.currentIndex) return
        setState { copy(currentIndex = index, comments = emptyList()) }
        recordIdAt(index)?.let(::loadComments)
    }

    /**
     * [recordId] 의 댓글 조회. 응답이 왔을 때 그 기록을 아직 보고 있을 때만 반영한다 —
     * 빠르게 넘기면 이전 페이지 응답이 늦게 와서 지금 페이지 댓글을 덮는다.
     */
    private fun loadComments(recordId: Long) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            setState { copy(isCommentsLoading = true) }
            val comments = try {
                loadAllComments(recordId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }
            if (recordIdAt(currentState.currentIndex) != recordId) return@launch
            setState { copy(comments = comments, isCommentsLoading = false) }
        }
    }

    /**
     * 그 기록의 댓글을 커서 끝까지 모아 오래된 → 최신 순으로 정렬한다(화면은 최신이 맨 아래).
     * 첫 페이지(기본 20개)만 받으면 서버 정렬에 따라 최신 댓글이 빠질 수 있다. 서버 정렬 방향에
     * 기대지 않도록 id(생성 순) 로 다시 정렬한다.
     */
    private suspend fun loadAllComments(recordId: Long): List<CommentUi> {
        val result = mutableListOf<Comment>()
        var cursor: Long? = null
        var pages = 0
        do {
            val page = feedRepository.getComments(groupId, recordId, cursor = cursor)
            result += page.comments
            cursor = page.nextCursor
            pages++
        } while (page.hasNext && cursor != null && pages < MAX_COMMENT_PAGES)
        return result.distinctBy { it.commentId }.sortedBy { it.commentId }.map { it.toUi() }
    }

    private fun sendComment() {
        val current = currentState
        if (!current.canSend) return
        postComment(content = current.commentInput.trim(), emoji = null)
    }

    /** 이모지는 탭하면 바로 전송. 입력 중인 글은 건드리지 않는다. */
    private fun sendEmoji(emoji: CommentEmoji) {
        if (currentState.isSending) return
        postComment(content = "", emoji = emoji)
    }

    /**
     * 글·이모지 공통 전송. 대상은 누른 순간 보고 있던 기록으로 고정한다.
     * 성공하면 글 입력을 비우고(글일 때), 그 기록을 아직 보고 있으면 목록을 다시 받는다.
     */
    private fun postComment(content: String, emoji: CommentEmoji?) = viewModelScope.launch {
        val recordId = recordIdAt(currentState.currentIndex) ?: return@launch
        setState { copy(isSending = true) }
        try {
            feedRepository.postComment(groupId, recordId, content, emojiId = emoji?.id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 글은 입력이 남아 다시 보낼 수 있지만, 이모지는 남는 게 없어 알려야 한다.
            setState { copy(isSending = false, sendError = "댓글을 보내지 못했어요. 다시 시도해주세요.") }
            return@launch
        }
        setState { copy(isSending = false, commentInput = if (emoji == null) "" else commentInput) }
        if (recordIdAt(currentState.currentIndex) == recordId) loadComments(recordId)
    }

    /**
     * 내 댓글 삭제. 대상 기록·댓글 id 는 시작할 때 붙든다.
     * 성공하면 그 기록을 아직 보고 있을 때만 목록에서 뺀다(넘겨버렸으면 다음에 다시 받을 때 반영된다).
     */
    private fun deleteComment() = viewModelScope.launch {
        val commentId = currentState.commentDeleteTargetId ?: return@launch
        val recordId = recordIdAt(currentState.currentIndex) ?: return@launch
        if (currentState.isDeletingComment) return@launch
        // 다이얼로그는 바로 닫고 오버레이로 대기를 보여준다.
        setState { copy(isDeletingComment = true, commentDeleteTargetId = null) }
        try {
            feedRepository.deleteComment(groupId, recordId, commentId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            setState {
                copy(isDeletingComment = false, commentDeleteError = "댓글을 삭제하지 못했어요. 다시 시도해주세요.")
            }
            return@launch
        }
        setState {
            copy(
                isDeletingComment = false,
                comments = if (recordIdAt(currentIndex) == recordId) comments.filterNot { it.id == commentId } else comments,
            )
        }
    }

    /** 현재 state 의 records 에서 index 위치 recordId. */
    private fun recordIdAt(index: Int): Long? = currentState.records.getOrNull(index)?.id

    private fun Comment.toUi(): CommentUi {
        val emoji = CommentEmoji.of(emojiId)
        return CommentUi(
            id = commentId,
            authorName = nickname,
            avatarUrl = profileImageUrl,
            // 본문이 비었는데 그릴 이모지가 없으면 빈 말풍선 대신 대체 문구. 글 댓글은 빈 문자열로
            // 보낼 수 없으니 이 경우는 이모지 댓글이다 — 모르는 아이디(구버전 앱)이거나, 서버가
            // emojiId 를 저장·반환하지 않은 경우(배포 전 서버).
            content = if (emoji == null && content.isBlank()) UNKNOWN_EMOJI_TEXT else content,
            isMine = isMine,
            emoji = emoji,
            timeLabel = createdAt?.let(::formatCommentTime),
        )
    }

    /** 오늘 쓴 댓글은 시각만, 그 전이면 날짜를 붙인다(기록 상세는 지난 날짜로도 들어온다). */
    private fun formatCommentTime(createdAt: Instant): String {
        val zone = ZoneId.systemDefault()
        val time = createdAt.atZone(zone)
        return if (time.toLocalDate() == LocalDate.now(zone)) {
            time.format(TIME_FORMATTER)
        } else {
            time.format(DATE_TIME_FORMATTER)
        }
    }

    private companion object {
        /** 상세 슬라이드 이어붙이기 안전 상한(무한 루프 방지). */
        const val MAX_DETAIL_PAGES = 20

        /** 댓글 이어받기 안전 상한(무한 루프 방지). 기본 20개 × 10 = 200개. */
        const val MAX_COMMENT_PAGES = 10

        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d HH:mm", Locale.KOREAN)

        /** 이 앱이 모르는 이모지 댓글의 대체 표시. */
        const val UNKNOWN_EMOJI_TEXT = "(이모지)"
    }
}
