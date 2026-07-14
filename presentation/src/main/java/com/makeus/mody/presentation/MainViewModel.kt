package com.makeus.mody.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.domain.session.SessionExpiredNotifier
import com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
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
 * 세션 완전 만료(재발급/무음 재로그인 실패) 이벤트를 받으면 로그인 화면으로 보낸다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val resolveStartDestination: ResolveStartDestinationUseCase,
    private val sessionExpiredNotifier: SessionExpiredNotifier,
    private val sessionRepository: SessionRepository,
    private val navigationHelper: NavigationHelper,
) : ViewModel() {

    private val _startRoute = MutableStateFlow<Route?>(null)
    val startRoute = _startRoute.asStateFlow()

    init {
        viewModelScope.launch {
            // 세션 조회 실패(DataStore/토큰) 시 스플래시에 갇히지 않도록 로그인으로 폴백
            val destination = runCatching { resolveStartDestination() }
                .getOrDefault(StartDestination.AUTH)
            _startRoute.value = when (destination) {
                StartDestination.AUTH -> AuthGraphBaseRoute
                StartDestination.ONBOARDING -> OnboardingGraphBaseRoute
                StartDestination.GROUP -> GroupGraphBaseRoute
                StartDestination.MAIN -> MainRoute
            }
        }
        viewModelScope.launch {
            sessionExpiredNotifier.events.collect {
                runCatching { sessionRepository.clear() }
                navigationHelper.navigate(
                    NavigationEvent.To(AuthGraphBaseRoute, popUpTo = true),
                )
            }
        }
    }
}
