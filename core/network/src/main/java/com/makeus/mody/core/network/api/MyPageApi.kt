package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.mypage.MyPageMeResponse
import com.makeus.mody.core.network.model.mypage.MyPageProfileResponse
import com.makeus.mody.core.network.model.mypage.MyPageProfileUpdateRequest
import com.makeus.mody.core.network.model.mypage.MyPageWeightCreateRequest
import com.makeus.mody.core.network.model.mypage.MyPageWeightCreateResponse
import com.makeus.mody.core.network.model.mypage.MyPageWeightsResponse
import com.makeus.mody.core.network.model.mypage.NotificationSettingRequest
import com.makeus.mody.core.network.model.mypage.NotificationSettingResponse
import com.makeus.mody.core.network.model.mypage.ScheduleRequest
import com.makeus.mody.core.network.model.mypage.ScheduleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface MyPageApi {

    /** 마이페이지 상단 프로필(닉네임·프로필사진·함께한 일수). */
    @GET("api/v1/mypage/me")
    suspend fun getMe(): ApiResponse<MyPageMeResponse>

    /** 체중 요약(이전·현재·목표). */
    @GET("api/v1/mypage/weights")
    suspend fun getWeights(): ApiResponse<MyPageWeightsResponse>

    /** 체중 기록 생성. */
    @POST("api/v1/mypage/weights")
    suspend fun createWeight(
        @Body request: MyPageWeightCreateRequest,
    ): ApiResponse<MyPageWeightCreateResponse>

    /** 프로필 상세(로그인 수단·이름·생년월일). */
    @GET("api/v1/mypage/profile")
    suspend fun getProfile(): ApiResponse<MyPageProfileResponse>

    /** 이름/생년월일 수정. */
    @PATCH("api/v1/mypage/profile")
    suspend fun updateProfile(
        @Body request: MyPageProfileUpdateRequest,
    ): ApiResponse<MyPageProfileResponse>

    /** 알림 설정 조회(토글 3개 + 식사/운동 스케줄). */
    @GET("api/v1/mypage/notification-settings")
    suspend fun getNotificationSettings(): ApiResponse<NotificationSettingResponse>

    /** 알림 토글 수정. */
    @PATCH("api/v1/mypage/notification-settings")
    suspend fun updateNotificationSettings(
        @Body request: NotificationSettingRequest,
    ): ApiResponse<NotificationSettingResponse>

    /** 식사/운동 스케줄 수정. */
    @PUT("api/v1/mypage/schedules")
    suspend fun updateSchedules(
        @Body request: ScheduleRequest,
    ): ApiResponse<ScheduleResponse>
}
