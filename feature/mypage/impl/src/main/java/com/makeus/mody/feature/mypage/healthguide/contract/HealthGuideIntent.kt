package com.makeus.mody.feature.mypage.healthguide.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class HealthGuideIntent : UiIntent {
    data object BackClicked : HealthGuideIntent()

    /** 페이저를 넘겨 다른 단계를 보고 있다. */
    data class StepChanged(val step: Int) : HealthGuideIntent()

    /** "건강 데이터 설정하러 가기" */
    data object OpenSettingsClicked : HealthGuideIntent()

    /** 설정 화면으로 보낸 뒤 요청 상태 소비. */
    data object SettingsLaunched : HealthGuideIntent()
}
