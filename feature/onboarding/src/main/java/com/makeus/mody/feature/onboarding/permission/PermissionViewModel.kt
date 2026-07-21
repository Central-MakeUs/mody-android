package com.makeus.mody.feature.onboarding.permission

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.onboarding.permission.contract.PermissionIntent
import com.makeus.mody.feature.onboarding.permission.contract.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<PermissionState, PermissionIntent>(PermissionState) {

    override suspend fun processIntent(intent: PermissionIntent) {
        when (intent) {
            // 권한은 선택이므로 허용 여부와 무관하게 그룹으로 진입.
            // 권한 화면 백스택 제거(뒤로가기로 복귀 방지).
            is PermissionIntent.Continue ->
                navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute, popUpTo = true))
        }
    }
}
