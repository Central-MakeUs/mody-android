package com.makeus.mody.feature.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.alarm.AlarmScreen
import com.makeus.mody.feature.onboarding.birth.BirthScreen
import com.makeus.mody.feature.onboarding.nickname.NicknameScreen
import com.makeus.mody.feature.onboarding.permission.PermissionScreen
import com.makeus.mody.feature.onboarding.weight.WeightScreen

fun NavGraphBuilder.onboardingNavGraph(navController: NavHostController) {
    navigation<OnboardingGraphBaseRoute>(startDestination = OnboardingGraph.NicknameRoute) {
        composable<OnboardingGraph.NicknameRoute> { entry ->
            NicknameScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.BirthRoute> { entry ->
            BirthScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.WeightRoute> { entry ->
            WeightScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.AlarmRoute> { entry ->
            AlarmScreen(entry.sharedViewModel(navController))
        }
        // 권한 요청 화면은 온보딩 입력값과 무관 → 공유 VM 대신 자체 hiltViewModel.
        composable<OnboardingGraph.PermissionRoute> {
            PermissionScreen()
        }
    }
}

/**
 * 온보딩 그래프 백스택 엔트리에 scope 된 단일 ViewModel.
 * 모든 스텝 화면이 같은 인스턴스를 공유해 입력값을 누적한다.
 */
@Composable
private fun NavBackStackEntry.sharedViewModel(
    navController: NavHostController,
): OnboardingViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(OnboardingGraphBaseRoute)
    }
    return hiltViewModel(parentEntry)
}
