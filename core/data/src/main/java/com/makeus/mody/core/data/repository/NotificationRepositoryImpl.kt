package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.Notification
import com.makeus.mody.core.domain.model.NotificationPage
import com.makeus.mody.core.domain.model.NotificationType
import com.makeus.mody.core.domain.repository.NotificationRepository
import com.makeus.mody.core.network.api.NotificationApi
import com.makeus.mody.core.network.model.notification.NotificationListResponse
import com.makeus.mody.core.network.model.notification.NotificationResponse
import com.makeus.mody.core.network.model.unwrapResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
) : NotificationRepository {

    override suspend fun getNotifications(cursor: Long?, size: Int?): NotificationPage =
        notificationApi.getNotifications(cursor = cursor, size = size)
            .unwrapResult()
            .toNotificationPage()

    override suspend fun readNotification(notificationId: Long) {
        notificationApi.readNotification(notificationId).unwrapResult()
    }
}

private fun NotificationListResponse.toNotificationPage(): NotificationPage =
    NotificationPage(
        notifications = notifications.map { it.toNotification() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

private fun NotificationResponse.toNotification(): Notification =
    Notification(
        notificationId = notificationId,
        type = type.toNotificationType(),
        title = title,
        description = description,
        createdAt = createdAt.parseServerInstant(),
        isRead = read,
    )

private fun String.toNotificationType(): NotificationType =
    runCatching { NotificationType.valueOf(this) }.getOrDefault(NotificationType.UNKNOWN)

// 서버 date-time. 오프셋 있으면(OffsetDateTime) 그대로, 없으면 로컬로 간주. 파싱 실패 시 EPOCH.
private fun String.parseServerInstant(): Instant = runCatching {
    OffsetDateTime.parse(this).toInstant()
}.recoverCatching {
    LocalDateTime.parse(this).atZone(ZoneId.systemDefault()).toInstant()
}.getOrDefault(Instant.EPOCH)
