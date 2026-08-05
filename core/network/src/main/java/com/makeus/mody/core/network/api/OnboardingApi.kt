package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.onboarding.HealthConnectionRequest
import com.makeus.mody.core.network.model.onboarding.HealthConnectionResponse
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileRequest
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface OnboardingApi {

    @POST("api/v1/onboarding/profile")
    suspend fun submitProfile(
        @Body request: OnboardingProfileRequest,
    ): ApiResponse<OnboardingProfileResponse>

    /** 건강 데이터(걸음 수) 연동 여부 기록. */
    @PUT("api/v1/onboarding/health-connection")
    suspend fun updateHealthConnection(
        @Body request: HealthConnectionRequest,
    ): ApiResponse<HealthConnectionResponse>
}
