package com.makeus.mody.feature.mypage.healthrationale.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class HealthRationaleIntent : UiIntent {
    data object BackClicked : HealthRationaleIntent()
    data object PrivacyPolicyClicked : HealthRationaleIntent()

    /** 브라우저를 띄운 뒤 요청 상태를 비운다(재실행 방지). */
    data object PrivacyPolicyLaunched : HealthRationaleIntent()
}
