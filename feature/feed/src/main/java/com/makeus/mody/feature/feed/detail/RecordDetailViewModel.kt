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
import com.makeus.mody.feature.feed.feed.contract.toFeedCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
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

    /** 상세 슬라이드 로드 → 진입 위치(currentIndex)의 기록 댓글 로드. */
    private fun loadDetail() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        runCatching { feedRepository.getRecordDetail(groupId, route.recordId) }
            .onSuccess { detail ->
                val index = detail.currentIndex.coerceIn(0, (detail.records.size - 1).coerceAtLeast(0))
                setState {
                    copy(
                        isLoading = false,
                        records = detail.records.map { it.toFeedCardUi() },
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

    private fun onPageChanged(index: Int) {
        if (index == currentState.currentIndex) return
        setState { copy(currentIndex = index, comments = emptyList()) }
        recordIdAt(index)?.let(::loadComments)
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
}
