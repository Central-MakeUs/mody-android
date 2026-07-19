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

/** GET /api/v1/notifications 응답. 커서 페이지네이션(인박스 알림 목록). */
@Serializable
data class NotificationListResponse(
    val notifications: List<NotificationResponse> = emptyList(),
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
)

/**
 * 인박스 알림 한 건.
 * @param type 서버 enum(GROUP_JOINED, COMMENT_CREATED 등). 화면 아이콘/이동 매핑 키.
 * @param createdAt ISO date-time 문자열(예: "2026-07-19T10:00:00").
 */
@Serializable
data class NotificationResponse(
    val notificationId: Long = 0,
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: String = "",
    val read: Boolean = false,
)
