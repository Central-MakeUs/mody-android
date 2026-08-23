package com.makeus.mody.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.PendingStreakTabHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val pendingGroupSelectionHolder: PendingGroupSelectionHolder,
    private val pendingStreakTabHolder: PendingStreakTabHolder,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.FEED)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    /** 노출할 하단 탭. */
    val visibleTabs: StateFlow<List<MainTab>> =
        MutableStateFlow(MainTab.entries.toList()).asStateFlow()

    init {
        // 원격 플래그 fetch(강제 업데이트·공지·심사용 히든 로그인).
        viewModelScope.launch { runCatching { remoteConfigRepository.refresh() } }
        // 그룹홈 알림으로 진입 시 Feed 탭으로 전환(그룹 선택은 FeedViewModel 이 소비).
        // 다른 탭에 있어도 그룹홈이 보이도록. consume 은 FeedViewModel 담당이라 여기선 peek 만.
        viewModelScope.launch {
            pendingGroupSelectionHolder.pendingGroupId.collect { groupId ->
                if (groupId != null) _selectedTab.value = MainTab.FEED
            }
        }
        // 피드 "콕 찌르기 하러 가기" → 챌린지 탭. 연속 기록 서브탭 전환은 ChallengeViewModel 이
        // 화면 진입 시 consume 해서 처리하므로 여기선 비우지 않는다.
        viewModelScope.launch {
            pendingStreakTabHolder.pending.collect { pending ->
                if (!pending) return@collect
                _selectedTab.value = MainTab.CHALLENGE
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    /**
     * 현재 탭을 화면 노출로 기록한다.
     *
     * MainActivity 의 목적지 추적은 Main 을 건너뛴다 — 컨테이너라서 실제로 보이는 건 탭이다.
     * 탭 전환뿐 아니라 다른 화면에 갔다 돌아온 경우에도 다시 불려야 해서, 호출 시점은
     * 화면(컴포지션)이 정한다.
     */
    fun trackCurrentTabScreen() {
        analyticsLogger.logScreenView(_selectedTab.value.screenName)
    }
}
