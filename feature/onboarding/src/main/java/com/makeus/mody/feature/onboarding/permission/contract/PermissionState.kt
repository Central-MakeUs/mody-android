package com.makeus.mody.feature.onboarding.permission.contract

import com.makeus.mody.core.commonui.base.UiState

data class PermissionState(
    /** Phase 2 기능 노출(Remote Config). Phase 1 에선 건강 정보(걸음 수 챌린지) 항목·권한 요청 제외. */
    val phaseTwoFeaturesEnabled: Boolean = false,
) : UiState
