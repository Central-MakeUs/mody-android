package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.mypage.MyPageMeResponse
import com.makeus.mody.core.network.model.mypage.MyPageWeightsResponse
import retrofit2.http.GET

interface MyPageApi {

    /** 마이페이지 상단 프로필(닉네임·프로필사진·함께한 일수). */
    @GET("api/v1/mypage/me")
    suspend fun getMe(): ApiResponse<MyPageMeResponse>

    /** 체중 요약(이전·현재·목표). */
    @GET("api/v1/mypage/weights")
    suspend fun getWeights(): ApiResponse<MyPageWeightsResponse>
}
