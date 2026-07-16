package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.notification.PushTokenDisableRequest
import com.makeus.mody.core.network.model.notification.PushTokenRegisterRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface NotificationApi {

    /** FCM 디바이스 토큰 등록/갱신. 서버가 이 토큰으로 푸시를 발송한다. */
    @POST("api/v1/notifications/push-token")
    suspend fun registerPushToken(
        @Body request: PushTokenRegisterRequest,
    ): ApiResponse<Unit>

    /**
     * 디바이스 토큰 비활성(로그아웃). DELETE + body 라 @HTTP 로 hasBody=true 지정.
     * (Retrofit @DELETE 는 바디를 못 실음)
     */
    @HTTP(method = "DELETE", path = "api/v1/notifications/push-token", hasBody = true)
    suspend fun disablePushToken(
        @Body request: PushTokenDisableRequest,
    ): ApiResponse<Unit>
}
