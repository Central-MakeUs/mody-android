package com.makeus.mody.feature.onboarding

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
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
                if (intent.day in 1..7) {
                    setState {
                        val next = exerciseDays.toMutableSet().apply {
                            if (!add(intent.day)) remove(intent.day)
                        }
                        copy(exerciseDays = next)
                    }
                }

            // 스텝 순서: 닉네임 → 생년월일 → 체중 → 알림 → 완료
            // 진행 불변조건을 reducer 에서 강제 (UI 게이팅 우회 방지)
            is OnboardingIntent.NicknameNext ->
                if (currentState.isNicknameValid) navigate(OnboardingGraph.BirthRoute)
            is OnboardingIntent.BirthNext -> navigate(OnboardingGraph.WeightRoute)
            is OnboardingIntent.WeightNext -> navigate(OnboardingGraph.AlarmRoute)
            is OnboardingIntent.AlarmNext ->
                if (currentState.isExerciseValid) complete()
        }
    }

    private fun navigate(route: OnboardingGraph) {
        navigationHelper.navigate(NavigationEvent.To(route))
    }

    private fun complete() {
        // 온보딩 완료 → 그룹 그래프로 핸드오프. 온보딩/로그인 백스택 제거(뒤로가기로 복귀 방지).
        navigationHelper.navigate(NavigationEvent.To(GroupGraphBaseRoute, popUpTo = true))
    }
}
