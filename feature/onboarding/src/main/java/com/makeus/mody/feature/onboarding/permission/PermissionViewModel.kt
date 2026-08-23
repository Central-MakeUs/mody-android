package com.makeus.mody.feature.onboarding.permission

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.onboarding.permission.contract.PermissionIntent
import com.makeus.mody.feature.onboarding.permission.contract.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
    private val healthRepository: HealthRepository,
    private val onboardingRepository: OnboardingRepository,
) : BaseViewModel<PermissionState, PermissionIntent>(PermissionState()) {

    init {
        // 기기 지원 여부는 바뀌지 않으므로 진입 시 한 번만 읽는다.
        setState {
            copy(healthAvailable = healthRepository.availability() == HealthAvailability.AVAILABLE)
        }
    }

    override suspend fun processIntent(intent: PermissionIntent) {
        when (intent) {
            is PermissionIntent.BasePermissionsHandled -> requestHealthOrContinue()
            is PermissionIntent.HealthPermissionRequestLaunched ->
                setState { copy(healthPermissionRequest = null) }
            is PermissionIntent.HealthPermissionResult -> onHealthPermissionResult(intent.granted)
        }
    }

    /**
     * 알림·카메라 다음 순서. 건강 권한은 Health Connect 의 별도 화면이라 앞의 요청과 겹치면
     * 안 돼 순차로 띄운다. 요청할 상황이 아니면 곧장 그룹으로 넘어간다.
     */
    private suspend fun requestHealthOrContinue() {
        if (!currentState.showHealth) {
            navigateToGroup()
            return
        }
        // 이미 허용된 상태면 다시 물어봐야 할 이유가 없다(재설치 후 재진입 등).
        if (runCatching { healthRepository.hasStepPermission() }.getOrDefault(false)) {
            navigateToGroup()
            return
        }
        // 여기선 "물어봤음" 플래그를 남기지 않는다. 남기면 온보딩에서 거부한 사용자가
        // 정작 걸음 수 챌린지를 처음 본 시점(챌린지 탭)에 자동 요청을 못 받는다 —
        // 걸음 수가 뭔지 모르는 시점의 거부가 기능을 이해한 시점의 기회를 먹는다.
        setState { copy(healthPermissionRequest = healthRepository.stepPermissions) }
    }

    /** 허용 여부와 무관하게 그룹으로 진입 — 선택 권한이라 여기서 흐름을 막지 않는다. */
    private fun onHealthPermissionResult(granted: Boolean) = viewModelScope.launch {
        setState { copy(healthPermissionRequest = null) }
        // 연동 여부 기록 실패는 사용자 흐름을 막을 이유가 없어 조용히 넘긴다(챌린지 탭과 동일).
        runCatching { onboardingRepository.reportHealthConnection(granted) }
        navigateToGroup()
    }

    /** 권한 화면은 백스택에서 지운다(뒤로가기로 복귀 방지). */
    private fun navigateToGroup() =
        navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute, popUpTo = true))
}
