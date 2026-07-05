package com.makeus.mody.core.data.mapper

import com.makeus.mody.core.domain.model.OnboardingProfile
import com.makeus.mody.core.network.model.onboarding.ExerciseScheduleRequest
import com.makeus.mody.core.network.model.onboarding.MealScheduleRequest
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileRequest

private val DAY_OF_WEEK = arrayOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
)

private fun time(hour: Int, minute: Int): String = "%02d:%02d:00".format(hour, minute)

/** 도메인 프로필 → 서버 요청 DTO. 날짜 yyyy-MM-dd, 시각 HH:mm:ss 문자열로 직렬화. */
fun OnboardingProfile.toRequest(): OnboardingProfileRequest = OnboardingProfileRequest(
    nickname = nickname,
    birthDate = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay),
    currentWeightKg = currentWeightKg.toDouble(),
    targetWeightKg = targetWeightKg.toDouble(),
    mealSchedules = meals.map {
        MealScheduleRequest(
            mealType = it.type.name,
            time = time(it.hour, it.minute),
            skipped = it.skipped,
        )
    },
    exerciseSchedules = exercises.map {
        ExerciseScheduleRequest(
            dayOfWeek = DAY_OF_WEEK[it.dayOfWeek - 1],
            time = time(it.hour, it.minute),
        )
    },
)
