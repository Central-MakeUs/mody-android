package com.makeus.mody.feature.onboarding.permission.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class PermissionIntent : UiIntent {
    /**
     * 알림·카메라 런타임 권한 요청이 끝났다(허용 여부 무관). 다음은 건강 권한 차례.
     * @param notificationGranted 알림 권한 응답. 요청하지 않은 OS(13 미만)면 null.
     */
    data class BasePermissionsHandled(val notificationGranted: Boolean?) : PermissionIntent()

    /** 건강 권한 요청 런처를 실행했다 — 재실행 방지로 요청 상태를 비운다. */
    data object HealthPermissionRequestLaunched : PermissionIntent()

    /**
     * 건강 권한 요청 결과. 런처를 띄우지 못한 경우도 `granted = false` 로 들어온다.
     * @param prompted 권한 화면이 실제로 떴는지. 못 띄운 경우는 사용자 응답이 아니다.
     */
    data class HealthPermissionResult(
        val granted: Boolean,
        val prompted: Boolean = true,
    ) : PermissionIntent()
}
