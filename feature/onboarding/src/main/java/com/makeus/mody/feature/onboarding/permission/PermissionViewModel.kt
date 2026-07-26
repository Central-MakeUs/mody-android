package com.makeus.mody.feature.onboarding.permission

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
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
    remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<PermissionState, PermissionIntent>(PermissionState()) {

    init {
        // Phase 2 기능 플래그 — Phase 1 에선 건강 정보(걸음 수 챌린지) 항목·권한 요청 제외.
        viewModelScope.launch {
            remoteConfigRepository.phaseTwoFeaturesEnabled.collect { enabled ->
                setState { copy(phaseTwoFeaturesEnabled = enabled) }
            }
        }
    }

    override suspend fun processIntent(intent: PermissionIntent) {
        when (intent) {
            // 권한은 선택이므로 허용 여부와 무관하게 그룹으로 진입.
            // 권한 화면 백스택 제거(뒤로가기로 복귀 방지).
            is PermissionIntent.Continue ->
                navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute, popUpTo = true))
        }
    }
}
