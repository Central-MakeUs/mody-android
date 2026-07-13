package com.makeus.mody.feature.feed.feed

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    @Suppress("unused") private val navigationHelper: NavigationHelper, // TODO(feed): 화면 이동 연결 시 사용
) : BaseViewModel<FeedState, FeedIntent>(FeedState()) {

    init {
        setState { copy(dateLabel = formatDate(LocalDate.now())) }
    }

    override suspend fun processIntent(intent: FeedIntent) {
        when (intent) {
            // TODO(feed): 각 액션 화면/API 연결
            is FeedIntent.DateClicked -> Unit
            is FeedIntent.MembersClicked -> Unit
            is FeedIntent.AlarmClicked -> Unit
            is FeedIntent.PokeClicked -> Unit
            is FeedIntent.WriteClicked -> Unit
        }
    }

    private fun formatDate(date: LocalDate): String = "${date.monthValue}월 ${date.dayOfMonth}일"
}
