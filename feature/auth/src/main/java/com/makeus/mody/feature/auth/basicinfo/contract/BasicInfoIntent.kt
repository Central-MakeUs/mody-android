package com.makeus.mody.feature.auth.basicinfo.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class BasicInfoIntent : UiIntent {
    data class NameChanged(val value: String) : BasicInfoIntent()
    data class BirthChanged(val value: String) : BasicInfoIntent()
    data class CurrentWeightChanged(val value: Float) : BasicInfoIntent()
    data class TargetWeightChanged(val value: Float) : BasicInfoIntent()
    data object NextClicked : BasicInfoIntent()
    data object BackClicked : BasicInfoIntent()
}
