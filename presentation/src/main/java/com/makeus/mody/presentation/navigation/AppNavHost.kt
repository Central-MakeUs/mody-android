package com.makeus.mody.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.makeus.mody.core.navigation.AuthGraphRoute
import com.makeus.mody.feature.auth.navigation.authNavGraph

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthGraphRoute,
    ) {
        authNavGraph()
    }
}
