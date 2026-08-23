package com.makeus.mody.feature.onboarding.permission.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class PermissionIntent : UiIntent {
    /** 알림·카메라 런타임 권한 요청이 끝났다(허용 여부 무관). 다음은 건강 권한 차례. */
    data object BasePermissionsHandled : PermissionIntent()

    /** 건강 권한 요청 런처를 실행했다 — 재실행 방지로 요청 상태를 비운다. */
    data object HealthPermissionRequestLaunched : PermissionIntent()

    /** 건강 권한 요청 결과. 런처를 띄우지 못한 경우도 `granted = false` 로 들어온다. */
    data class HealthPermissionResult(val granted: Boolean) : PermissionIntent()
}
