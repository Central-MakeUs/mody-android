package com.makeus.mody.feature.mypage.groupsetting.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.domain.model.Group

sealed class GroupSettingIntent : UiIntent {
    data object BackClicked : GroupSettingIntent()
    data class LeaveClicked(val group: Group) : GroupSettingIntent()
    data object LeaveConfirmed : GroupSettingIntent()
    data object LeaveDismissed : GroupSettingIntent()
    data object ErrorShown : GroupSettingIntent()
}
