package com.makeus.mody.feature.onboarding

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<OnboardingState, OnboardingIntent>(OnboardingState()) {

    override suspend fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NicknameChanged ->
                setState { copy(nickname = intent.value) }

            is OnboardingIntent.BirthChanged ->
                setState { copy(birthYear = intent.year, birthMonth = intent.month, birthDay = intent.day) }

            is OnboardingIntent.WeightChanged ->
                setState { copy(currentWeight = intent.current, targetWeight = intent.target) }

            is OnboardingIntent.MealHourChanged ->
                setState {
                    copy(
                        breakfastHour = intent.breakfast,
                        lunchHour = intent.lunch,
                        dinnerHour = intent.dinner,
                    )
                }

            is OnboardingIntent.ExerciseDayToggled ->
                setState {
                    val next = exerciseDays.toMutableSet().apply {
                        if (!add(intent.day)) remove(intent.day)
                    }
                    copy(exerciseDays = next)
                }

            is OnboardingIntent.NicknameNext -> navigate(OnboardingGraph.BirthRoute)
            is OnboardingIntent.BirthNext -> navigate(OnboardingGraph.WeightRoute)
            is OnboardingIntent.WeightNext -> navigate(OnboardingGraph.AlarmRoute)
            is OnboardingIntent.AlarmNext -> complete()
        }
    }

    private fun navigate(route: OnboardingGraph) {
        navigationHelper.navigate(NavigationEvent.To(route))
    }

    private fun complete() {
        // TODO: currentState 프로필을 repository 로 저장 후 메인 그래프로 이동
    }
}
