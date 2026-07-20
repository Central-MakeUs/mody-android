package com.makeus.mody.feature.notification.notification.contract

import androidx.annotation.DrawableRes

/** 알림 목록 한 행. 아이콘/시간은 ViewModel 에서 도메인 모델을 가공해 채운다. */
data class NotificationUiModel(
    val id: Long,
    @DrawableRes val iconRes: Int,
    val title: String,
    val description: String,
    val timeText: String,
    val isRead: Boolean,
)
