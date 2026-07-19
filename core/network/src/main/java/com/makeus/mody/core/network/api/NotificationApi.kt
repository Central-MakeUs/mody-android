package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.notification.NotificationListResponse
import com.makeus.mody.core.network.model.notification.PushTokenDisableRequest
import com.makeus.mody.core.network.model.notification.PushTokenRegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    /** 인박스 알림 목록. cursor=null 이면 첫 페이지. */
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<NotificationListResponse>

    /** 알림 단건 읽음 처리. */
    @PATCH("api/v1/notifications/{notificationId}/read")
    suspend fun readNotification(
        @Path("notificationId") notificationId: Long,
    ): ApiResponse<Unit>

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
