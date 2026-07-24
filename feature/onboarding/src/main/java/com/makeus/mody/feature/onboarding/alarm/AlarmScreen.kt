package com.makeus.mody.feature.onboarding.alarm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.MealExerciseSchedule
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState

@Composable
fun AlarmScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScaffold(
        stepIndex = 3,
        totalSteps = 4,
        title = "식사와 운동 알림을\n언제 드릴까요?",
        nextEnabled = state.isExerciseValid,
        onNextClick = { viewModel.onIntent(OnboardingIntent.AlarmNext) },
    ) {
        MealExerciseSchedule(
            breakfastHour = state.breakfastHour,
            lunchHour = state.lunchHour,
            dinnerHour = state.dinnerHour,
            onMealHoursChange = { b, l, d ->
                viewModel.onIntent(OnboardingIntent.MealHourChanged(b, l, d))
            },
            exerciseTimes = state.exerciseTimes.mapValues { (_, t) -> t.hour to t.minute },
            onExerciseDaySet = { day, h, m ->
                viewModel.onIntent(OnboardingIntent.ExerciseDaySet(day, h, m))
            },
            onExerciseDayRemoved = { day ->
                viewModel.onIntent(OnboardingIntent.ExerciseDayRemoved(day))
            },
            onExerciseAllTimesSet = { h, m ->
                viewModel.onIntent(OnboardingIntent.ExerciseAllTimesSet(h, m))
            },
            minExerciseDays = OnboardingState.EXERCISE_MIN_DAYS,
        )
    }
}
