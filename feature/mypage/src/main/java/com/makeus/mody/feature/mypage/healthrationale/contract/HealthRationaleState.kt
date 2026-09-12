package com.makeus.mody.feature.mypage.healthrationale.contract

import com.makeus.mody.core.commonui.base.UiState

data class HealthRationaleState(
    /**
     * 값이 있으면 이 주소로 개인정보처리방침을 연다(일회성).
     * 실행 후 [HealthRationaleIntent.PrivacyPolicyLaunched] 로 비운다.
     */
    val privacyPolicyRequest: String? = null,
) : UiState
