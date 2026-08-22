package com.makeus.mody.feature.onboarding.permission.contract

import com.makeus.mody.core.commonui.base.UiState

data class PermissionState(
    /** Phase 2 기능 노출(Remote Config). Phase 1 에선 건강 정보(걸음 수 챌린지) 항목·권한 요청 제외. */
    val phaseTwoFeaturesEnabled: Boolean = false,
    /**
     * 이 기기에서 Health Connect 를 쓸 수 있는지.
     * 미설치·구버전이면 권한 요청 자체가 불가능하므로 항목도 숨기고 건너뛴다.
     */
    val healthAvailable: Boolean = false,
    /**
     * 값이 있으면 이 권한들로 Health Connect 권한 요청을 띄운다(일회성).
     * 런처 실행 후 [PermissionIntent.HealthPermissionRequestLaunched] 로 비운다.
     */
    val healthPermissionRequest: Set<String>? = null,
) : UiState {

    /**
     * 건강 정보 항목 노출 여부.
     *
     * 표기와 실제 요청을 같은 조건으로 묶는다 — 예전엔 항목만 그리고 권한은 한 번도 묻지
     * 않아, 화면이 약속한 기능이 앱 어디에도 없는 상태였다(스토어 심사 반려 사유).
     */
    val showHealth: Boolean get() = phaseTwoFeaturesEnabled && healthAvailable
}
