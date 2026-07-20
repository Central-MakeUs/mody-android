package com.makeus.mody.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
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
) : ViewModel() {

    private companion object {
        const val TAG = "MainScreenViewModel"
    }

    private val _selectedTab = MutableStateFlow(MainTab.FEED)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    /** 노출할 하단 탭. 챌린지는 Remote Config 플래그가 켜졌을 때만 포함. */
    val visibleTabs: StateFlow<List<MainTab>> = remoteConfigRepository.challengeEnabled
        .map { challengeEnabled ->
            MainTab.entries.filter { it != MainTab.CHALLENGE || challengeEnabled }
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
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
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
