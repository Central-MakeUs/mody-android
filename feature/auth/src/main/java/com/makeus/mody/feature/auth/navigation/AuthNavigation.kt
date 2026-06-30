package com.makeus.mody.feature.auth.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.makeus.mody.core.navigation.AuthGraph
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.feature.auth.login.LoginScreen

fun NavGraphBuilder.authNavGraph() {
    navigation<AuthGraphBaseRoute>(startDestination = AuthGraph.LoginRoute) {
        composable<AuthGraph.LoginRoute> { LoginScreen() }
        composable<AuthGraph.SignUpRoute> { SignUpScreen() }
    }
}

@Composable
private fun SignUpScreen() {
    Text(text = "SignUpScreen")
}
