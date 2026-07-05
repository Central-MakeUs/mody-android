package com.makeus.mody.core.data.repository

import com.makeus.mody.core.data.mapper.toRequest
import com.makeus.mody.core.domain.model.OnboardingProfile
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.api.OnboardingApi
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi,
    private val sessionRepository: SessionRepository,
) : OnboardingRepository {

    override suspend fun submitProfile(profile: OnboardingProfile) {
        val response = onboardingApi.submitProfile(profile.toRequest()).unwrapResult()
        // 서버 확정값으로 세션 상태 갱신 → 재접속 시 시작 라우팅이 GROUP 로 감
        sessionRepository.saveStatus(
            sessionRepository.getStatus().copy(
                personalInfoCompleted = response.personalInfoCompleted,
            ),
        )
    }
}
