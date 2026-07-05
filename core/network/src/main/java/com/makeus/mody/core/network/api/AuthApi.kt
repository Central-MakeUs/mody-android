package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.auth.OAuthRedirectUrlResponse
import com.makeus.mody.core.network.model.auth.SocialLoginResponse
import com.makeus.mody.core.network.model.auth.TokenLogoutRequest
import com.makeus.mody.core.network.model.auth.TokenReissueRequest
import com.makeus.mody.core.network.model.auth.TokenReissueResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    /**
     * 소셜 SDK 로 받은 accessToken 을 서버에 넘겨 우리 JWT + 진행 상태를 받는다.
     * loginType: "kakao" | "google"
     */
    @GET("api/v1/oauth/client/{loginType}")
    suspend fun clientLogin(
        @Path("loginType") loginType: String,
        @Query("accessToken") socialAccessToken: String,
    ): ApiResponse<SocialLoginResponse>

    @GET("api/v1/oauth/{loginType}/redirect-url")
    suspend fun getRedirectUrl(
        @Path("loginType") loginType: String,
    ): ApiResponse<OAuthRedirectUrlResponse>

    @POST("api/v1/auth/reissue")
    suspend fun reissue(
        @Body request: TokenReissueRequest,
    ): ApiResponse<TokenReissueResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: TokenLogoutRequest,
    ): ApiResponse<Unit>
}
