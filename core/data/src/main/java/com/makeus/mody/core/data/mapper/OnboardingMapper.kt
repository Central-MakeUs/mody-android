package com.makeus.mody.core.data.mapper

import com.makeus.mody.core.domain.model.OnboardingProfile
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileRequest

/** 도메인 프로필 → 서버 요청 DTO. 날짜 yyyy-MM-dd, 스케줄 매핑은 [ScheduleMapper] 공용 사용. */
fun OnboardingProfile.toRequest(): OnboardingProfileRequest = OnboardingProfileRequest(
    nickname = nickname,
    birthDate = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay),
    currentWeightKg = currentWeightKg.toDouble(),
    targetWeightKg = targetWeightKg.toDouble(),
    mealSchedules = meals.map { it.toItem() },
    exerciseSchedules = exercises.map { it.toItem() },
)
