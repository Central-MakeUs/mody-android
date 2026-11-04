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
import com.makeus.mody.feature.feed.detail.contract.CommentUi
import com.makeus.mody.feature.feed.detail.contract.RecordDetailIntent
import com.makeus.mody.feature.feed.detail.contract.RecordDetailState
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi
import com.makeus.mody.feature.feed.feed.contract.toFeedCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
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

    /** 페이지 스와이프 = 같은 게시물의 다른 사진. 댓글은 게시물 단위로 통일이라 재조회하지 않고 index 만 갱신. */
    private fun onPageChanged(index: Int) {
        if (index == currentState.currentIndex) return
        setState { copy(currentIndex = index) }
    }

    private fun loadComments(recordId: Long) = viewModelScope.launch {
        setState { copy(isCommentsLoading = true) }
        runCatching { feedRepository.getComments(groupId, recordId) }
            .onSuccess { page ->
                setState { copy(comments = page.comments.map { it.toUi() }, isCommentsLoading = false) }
            }
            .onFailure { setState { copy(comments = emptyList(), isCommentsLoading = false) } }
    }

    private fun sendComment() = viewModelScope.launch {
        val current = currentState
        if (!current.canSend) return@launch
        val recordId = recordIdAt(current.currentIndex) ?: return@launch
        val content = current.commentInput.trim()
        setState { copy(isSending = true) }
        runCatching { feedRepository.postComment(groupId, recordId, content) }
            .onSuccess {
                setState { copy(commentInput = "", isSending = false) }
                loadComments(recordId) // 목록 갱신
            }
            .onFailure {
                // TODO(feed): 전송 실패 노출. 지금은 입력 유지.
                setState { copy(isSending = false) }
            }
    }

    /** 현재 state 의 records 에서 index 위치 recordId. */
    private fun recordIdAt(index: Int): Long? = currentState.records.getOrNull(index)?.id

    private fun Comment.toUi(): CommentUi = CommentUi(
        id = commentId,
        authorName = nickname,
        avatarUrl = profileImageUrl,
        content = content,
        isMine = isMine,
    )

    private companion object {
        /** 상세 슬라이드 이어붙이기 안전 상한(무한 루프 방지). */
        const val MAX_DETAIL_PAGES = 20
    }
}
