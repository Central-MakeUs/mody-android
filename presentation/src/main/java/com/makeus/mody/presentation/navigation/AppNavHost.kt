package com.makeus.mody.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.Route
import com.makeus.mody.feature.auth.navigation.authNavGraph
import com.makeus.mody.feature.feed.navigation.feedNavGraph
import com.makeus.mody.feature.group.navigation.groupNavGraph
import com.makeus.mody.feature.mypage.navigation.myPageNavGraph
import com.makeus.mody.feature.notification.navigation.notificationNavGraph
import com.makeus.mody.feature.onboarding.navigation.onboardingNavGraph
import com.makeus.mody.feature.record.navigation.recordNavGraph
import com.makeus.mody.presentation.main.MainScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Route,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authNavGraph()
        onboardingNavGraph(navController)
        groupNavGraph(navController)
        recordNavGraph()
        notificationNavGraph()
        myPageNavGraph()
        feedNavGraph()
        composable<MainRoute> { MainScreen() }
    }
}
