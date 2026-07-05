package com.makeus.mody.core.network.model.onboarding

import kotlinx.serialization.Serializable

/** POST /api/v1/onboarding/profile 요청 바디. */
@Serializable
data class OnboardingProfileRequest(
    val nickname: String,
    val birthDate: String, // yyyy-MM-dd
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val mealSchedules: List<MealScheduleRequest>,
    val exerciseSchedules: List<ExerciseScheduleRequest>,
)

@Serializable
data class MealScheduleRequest(
    val mealType: String, // BREAKFAST | LUNCH | DINNER
    val time: String, // HH:mm:ss
    val skipped: Boolean,
)

@Serializable
data class ExerciseScheduleRequest(
    val dayOfWeek: String, // MONDAY ~ SUNDAY
    val time: String, // HH:mm:ss
)

@Serializable
data class OnboardingProfileResponse(
    val memberId: Long = 0,
    val weightRecordId: Long = 0,
    val personalInfoCompleted: Boolean = false,
)
