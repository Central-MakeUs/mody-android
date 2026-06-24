package com.makeus.mody.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
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
                        }
                        is NavigationEvent.Up -> navController.navigateUp()
                    }
                }
            }

            ModyTheme {
                AppNavHost(navController = navController)
            }
        }
    }
}
