package com.makeus.mody.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.feature.auth.navigation.authNavGraph
import com.makeus.mody.feature.group.navigation.groupNavGraph
import com.makeus.mody.feature.onboarding.navigation.onboardingNavGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AuthGraphBaseRoute,
        modifier = modifier,
    ) {
        authNavGraph()
        onboardingNavGraph(navController)
        groupNavGraph()
        composable<MainRoute> { MainScreen() }
    }
}

@Composable
private fun MainScreen() {
    Text(text = "Main")
}
