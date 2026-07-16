package com.makeus.mody.core.network.model.notification

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/notifications/push-token 요청 바디.
 * @param platform 서버 enum. 안드로이드는 항상 "ANDROID".
 */
@Serializable
data class PushTokenRegisterRequest(
    val deviceId: String,
    val platform: String,
    val fcmToken: String,
)

/** DELETE /api/v1/notifications/push-token 요청 바디(로그아웃 시 해당 기기 토큰 비활성). */
@Serializable
data class PushTokenDisableRequest(
    val deviceId: String,
)
