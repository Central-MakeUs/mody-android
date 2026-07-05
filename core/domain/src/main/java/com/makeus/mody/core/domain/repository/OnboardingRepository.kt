package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.OnboardingProfile

/**
 * 온보딩 프로필 제출.
 * 구현체는 서버 성공 응답으로 세션 상태(personalInfoCompleted)를 갱신한다.
 */
interface OnboardingRepository {
    suspend fun submitProfile(profile: OnboardingProfile)
}
