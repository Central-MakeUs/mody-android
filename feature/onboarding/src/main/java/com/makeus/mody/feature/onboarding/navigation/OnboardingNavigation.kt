package com.makeus.mody.feature.onboarding.navigation

import androidx.compose.material3.Text
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
import com.makeus.mody.feature.onboarding.weight.WeightScreen

fun NavGraphBuilder.onboardingNavGraph(navController: NavHostController) {
    navigation<OnboardingGraphBaseRoute>(startDestination = OnboardingGraph.HeightWeightInputRoute) {
        composable<OnboardingGraph.HeightWeightInputRoute> { entry ->
            HeightWeightInputScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.MealAlarmTimeRoute> { entry ->
            MealAlarmTimeScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.ExerciseAlarmTimeRoute> { entry ->
            ExerciseAlarmTimeScreen(entry.sharedViewModel(navController))
        }
        composable<OnboardingGraph.OnboardingCompleteRoute> {
            OnboardingCompleteScreen()
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

@Composable
private fun HeightWeightInputScreen(viewModel: OnboardingViewModel) {
    WeightScreen(viewModel)
}

@Composable
private fun MealAlarmTimeScreen(viewModel: OnboardingViewModel) {
    AlarmScreen(viewModel)
}

@Composable
private fun ExerciseAlarmTimeScreen(viewModel: OnboardingViewModel) {
    AlarmScreen(viewModel)
}

@Composable
private fun OnboardingCompleteScreen() {
    Text(text = "OnboardingCompleteScreen")
}
