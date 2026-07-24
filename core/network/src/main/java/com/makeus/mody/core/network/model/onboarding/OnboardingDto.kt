package com.makeus.mody.core.network.model.onboarding

import com.makeus.mody.core.network.model.schedule.ExerciseScheduleItem
import com.makeus.mody.core.network.model.schedule.MealScheduleItem
import kotlinx.serialization.Serializable

/** POST /api/v1/onboarding/profile 요청 바디. 스케줄은 마이페이지와 공용 DTO. */
@Serializable
data class OnboardingProfileRequest(
    val nickname: String,
    val birthDate: String, // yyyy-MM-dd
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val mealSchedules: List<MealScheduleItem>,
    val exerciseSchedules: List<ExerciseScheduleItem>,
)

@Serializable
data class OnboardingProfileResponse(
    val memberId: Long = 0,
    val weightRecordId: Long = 0,
    val personalInfoCompleted: Boolean = false,
)
