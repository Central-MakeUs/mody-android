package com.makeus.mody.feature.notification.notification.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class NotificationIntent : UiIntent {
    data object BackClicked : NotificationIntent()
}
