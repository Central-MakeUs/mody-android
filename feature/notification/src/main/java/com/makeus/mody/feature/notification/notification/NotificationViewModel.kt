package com.makeus.mody.feature.notification.notification

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.notification.notification.contract.NotificationIntent
import com.makeus.mody.feature.notification.notification.contract.NotificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<NotificationState, NotificationIntent>(NotificationState()) {

    override suspend fun processIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
        }
    }
}
