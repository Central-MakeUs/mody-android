package com.makeus.mody.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val navigationHelper: NavigationHelper,
) : ViewModel() {

    private companion object {
        const val TAG = "MainScreenViewModel"
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

    // TODO(temp): 개발 중 화면 이동 확인용 임시 버튼. 플로우 완성 후 제거.
    fun goToGroup() {
        navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute))
    }

    fun goToOnboarding() {
        navigationHelper.navigate(NavigationEvent.To(OnboardingGraphBaseRoute))
    }
}
