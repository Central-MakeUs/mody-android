package com.makeus.mody.feature.feed.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.Comment
import com.makeus.mody.core.domain.repository.FeedRepository
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
import kotlinx.coroutines.launch

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val navigationHelper: NavigationHelper,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<RecordDetailState, RecordDetailIntent>(RecordDetailState()) {

    private val route = savedStateHandle.toRoute<FeedGraph.RecordDetailRoute>()
    private val groupId = route.groupId
    private val date = LocalDate.parse(route.date)

    init {
        loadDetail()
    }

    override suspend fun processIntent(intent: RecordDetailIntent) {
        when (intent) {
            is RecordDetailIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
            is RecordDetailIntent.PageChanged -> onPageChanged(intent.index)
            is RecordDetailIntent.CommentInputChanged ->
                setState { copy(commentInput = intent.text) }
            is RecordDetailIntent.SendCommentClicked -> sendComment()
        }
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
