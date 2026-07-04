package com.makeus.mody.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.presentation.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var navigationHelper: NavigationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                navigationHelper.navigationFlow.collect { event ->
                    when (event) {
                        is NavigationEvent.To -> navController.navigate(event.route) {
                            if (event.popUpTo) popUpTo(0) { inclusive = true }
                            // 같은 목적지가 스택 최상단이면 중복 push 방지(빠른 연타 대응)
                            launchSingleTop = true
                        }
                        is NavigationEvent.Up -> navController.navigateUp()
                        is NavigationEvent.TopLevelTo -> navController.navigate(event.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }

            ModyTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val startRoute by mainViewModel.startRoute.collectAsState()

                // startRoute 판정 전에는 스플래시(빈 화면). 판정되면 그 목적지로 NavHost 구성.
                val route = startRoute
                if (route == null) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(ModyTheme.colors.white))
                } else {
                    AppNavHost(navController = navController, startDestination = route)
                }
            }
        }
    }
}
