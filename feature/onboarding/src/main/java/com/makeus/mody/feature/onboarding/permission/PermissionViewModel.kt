package com.makeus.mody.feature.onboarding.permission

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.domain.repository.SessionRepository
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
    private val sessionRepository: SessionRepository,
    private val onboardingRepository: OnboardingRepository,
    remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<PermissionState, PermissionIntent>(PermissionState()) {

    init {
        // Phase 2 기능 플래그 — Phase 1 에선 건강 정보(걸음 수 챌린지) 항목·권한 요청 제외.
        viewModelScope.launch {
            remoteConfigRepository.phaseTwoFeaturesEnabled.collect { enabled ->
                setState { copy(phaseTwoFeaturesEnabled = enabled) }
            }
        }
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
        // 챌린지 탭이 "탭 진입마다 팝업"을 막는 데 쓰는 플래그와 같은 것. 여기서 물어봤으면
        // 탭 진입 때 또 묻지 않는다(수동 새로고침은 그대로 다시 묻는다).
        runCatching { sessionRepository.saveHealthPermissionAsked() }
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
