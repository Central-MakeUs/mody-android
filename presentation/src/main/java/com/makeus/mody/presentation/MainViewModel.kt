package com.makeus.mody.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.core.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱 시작 시 진입 목적지 결정.
 * 세션 상태(로그인/온보딩)를 읽어 도메인 결과를 네비게이션 Route 로 매핑.
 * startRoute == null 이면 아직 판정 중(스플래시).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val resolveStartDestination: ResolveStartDestinationUseCase,
) : ViewModel() {

    private val _startRoute = MutableStateFlow<Route?>(null)
    val startRoute = _startRoute.asStateFlow()

    init {
        viewModelScope.launch {
            _startRoute.value = when (resolveStartDestination()) {
                StartDestination.AUTH -> AuthGraphBaseRoute
                StartDestination.ONBOARDING -> OnboardingGraphBaseRoute
                StartDestination.GROUP -> GroupGraphBaseRoute
                StartDestination.MAIN -> MainRoute
            }
        }
    }
}
