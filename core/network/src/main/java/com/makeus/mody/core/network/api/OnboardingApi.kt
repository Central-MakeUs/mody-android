package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileRequest
import com.makeus.mody.core.network.model.onboarding.OnboardingProfileResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OnboardingApi {

    @POST("api/v1/onboarding/profile")
    suspend fun submitProfile(
        @Body request: OnboardingProfileRequest,
    ): ApiResponse<OnboardingProfileResponse>
}
