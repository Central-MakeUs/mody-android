package com.makeus.mody.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.makeus.mody.core.navigation.AuthGraph
import com.makeus.mody.core.navigation.AuthGraphRoute
import com.makeus.mody.feature.auth.basicinfo.BasicInfoScreen
import com.makeus.mody.feature.auth.group.GroupScreen
import com.makeus.mody.feature.auth.login.LoginScreen

fun NavGraphBuilder.authNavGraph() {
    navigation<AuthGraphRoute>(startDestination = AuthGraph.LoginRoute) {
        composable<AuthGraph.LoginRoute> { LoginScreen() }
        composable<AuthGraph.BasicInfoRoute> { BasicInfoScreen() }
        composable<AuthGraph.GroupRoute> { GroupScreen() }
    }
}
