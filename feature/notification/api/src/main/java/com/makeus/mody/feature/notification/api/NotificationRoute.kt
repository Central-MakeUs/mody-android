package com.makeus.mody.feature.notification.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object NotificationGraphBaseRoute : Route

sealed interface NotificationGraph : Route {
    /** 알림 목록 */
    @Serializable
    data object NotificationRoute : NotificationGraph
}
