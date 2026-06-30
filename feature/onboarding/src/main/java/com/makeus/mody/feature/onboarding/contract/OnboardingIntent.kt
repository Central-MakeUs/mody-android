package com.makeus.mody.feature.onboarding.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class OnboardingIntent : UiIntent {
    // 입력 변경
    data class NicknameChanged(val value: String) : OnboardingIntent()
    data class BirthChanged(val year: Int, val month: Int, val day: Int) : OnboardingIntent()
    data class WeightChanged(val current: Int, val target: Int) : OnboardingIntent()
    // null = 해당 끼니 "식사 안 함"
    data class MealHourChanged(
        val breakfast: Int?,
        val lunch: Int?,
        val dinner: Int?,
    ) : OnboardingIntent()
    data class ExerciseDayToggled(val day: Int) : OnboardingIntent()

    // 스텝 이동 ("다음으로")
    data object NicknameNext : OnboardingIntent()
    data object BirthNext : OnboardingIntent()
    data object WeightNext : OnboardingIntent()
    data object AlarmNext : OnboardingIntent() // 마지막 → 완료
}
