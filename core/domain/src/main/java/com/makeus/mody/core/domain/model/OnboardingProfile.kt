package com.makeus.mody.core.domain.model

/**
 * 온보딩에서 수집한 사용자 프로필. 서버 POST /onboarding/profile 제출 단위.
 * dayOfWeek: 1(월) ~ 7(일).
 */
data class OnboardingProfile(
    val nickname: String,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val currentWeightKg: Int,
    val targetWeightKg: Int,
    val meals: List<MealSchedule>,
    val exercises: List<ExerciseSchedule>,
)

enum class MealType { BREAKFAST, LUNCH, DINNER }

/** skipped=true 면 해당 끼니 알림 끔("식사 안 함"). */
data class MealSchedule(
    val type: MealType,
    val hour: Int,
    val minute: Int,
    val skipped: Boolean,
)

data class ExerciseSchedule(
    val dayOfWeek: Int, // 1(월) ~ 7(일)
    val hour: Int,
    val minute: Int,
)
