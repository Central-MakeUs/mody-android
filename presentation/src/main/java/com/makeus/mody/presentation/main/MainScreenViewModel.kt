package com.makeus.mody.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.core.navigation.PendingStreakTabHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val navigationHelper: NavigationHelper,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val pendingGroupSelectionHolder: PendingGroupSelectionHolder,
    private val pendingStreakTabHolder: PendingStreakTabHolder,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {

    private companion object {
        const val TAG = "MainScreenViewModel"
    }

    private val _selectedTab = MutableStateFlow(MainTab.FEED)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    /** 노출할 하단 탭. 챌린지는 Remote Config 플래그가 켜졌을 때만 포함. */
    val visibleTabs: StateFlow<List<MainTab>> = remoteConfigRepository.phaseTwoFeaturesEnabled
        .map { phaseTwoEnabled ->
            MainTab.entries.filter { it != MainTab.CHALLENGE || phaseTwoEnabled }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainTab.entries.filter { it != MainTab.CHALLENGE },
        )

    init {
        // 원격 플래그 fetch(실패해도 기본 숨김 유지).
        viewModelScope.launch { runCatching { remoteConfigRepository.refresh() } }
        // 챌린지가 숨겨졌는데 선택돼 있으면 피드로 복귀.
        viewModelScope.launch {
            visibleTabs.collect { tabs ->
                if (_selectedTab.value !in tabs) _selectedTab.value = MainTab.FEED
            }
        }
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
                // 챌린지 탭이 숨겨진 상태(Phase 1)면 갈 곳이 없다. 요청이 남아 다음 진입에
                // 엉뚱하게 소비되지 않도록 여기서 버린다.
                if (MainTab.CHALLENGE in visibleTabs.value) {
                    _selectedTab.value = MainTab.CHALLENGE
                } else {
                    pendingStreakTabHolder.consume()
                }
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

    fun logout() {
        viewModelScope.launch {
            // authRepository.logout() 은 내부에서 서버 통지 실패해도 로컬 세션을 clear 한다.
            runCatching { authRepository.logout() }
                .onFailure { Log.w(TAG, "logout 서버 통지 실패(로컬 세션은 clear됨)", it) }
            val navigated = navigationHelper.navigate(NavigationEvent.To(AuthGraphBaseRoute, popUpTo = true))
            if (!navigated) {
                Log.w(TAG, "로그아웃 후 네비게이션 이벤트가 드롭됨")
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            // 서버 탈퇴 성공해야 세션 clear + 로그인으로. 실패하면 로그만 남기고 화면 유지.
            val result = runCatching { authRepository.withdraw() }
                .onFailure { Log.w(TAG, "회원탈퇴 실패", it) }
            if (result.isSuccess) {
                navigationHelper.navigate(NavigationEvent.To(AuthGraphBaseRoute, popUpTo = true))
            }
        }
    }

    // TODO(temp): 개발 중 화면 이동 확인용 임시 버튼. 플로우 완성 후 제거.
    fun goToGroup() {
        navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute))
    }

    fun goToOnboarding() {
        navigationHelper.navigate(NavigationEvent.To(OnboardingGraphBaseRoute))
    }
}
