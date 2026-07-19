package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.NotificationPage

/** 인박스 알림 화면 데이터. */
interface NotificationRepository {
    /** 알림 목록. cursor=null 이면 첫 페이지. */
    suspend fun getNotifications(cursor: Long? = null, size: Int? = null): NotificationPage

    /** 알림 단건 읽음 처리. */
    suspend fun readNotification(notificationId: Long)
}
