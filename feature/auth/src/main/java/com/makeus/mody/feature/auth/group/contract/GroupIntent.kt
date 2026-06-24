package com.makeus.mody.feature.auth.group.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class GroupIntent : UiIntent {
    data class GroupCodeChanged(val value: String) : GroupIntent()
    data class GroupNameChanged(val value: String) : GroupIntent()
    data object JoinClicked : GroupIntent()
    data object CreateClicked : GroupIntent()
}
