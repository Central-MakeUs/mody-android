package com.makeus.mody.feature.notification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.core.navigation.NotificationGraphBaseRoute
import com.makeus.mody.feature.notification.notification.NotificationScreen

fun NavGraphBuilder.notificationNavGraph() {
    navigation<NotificationGraphBaseRoute>(startDestination = NotificationGraph.NotificationRoute) {
        composable<NotificationGraph.NotificationRoute> { NotificationScreen() }
    }
}
