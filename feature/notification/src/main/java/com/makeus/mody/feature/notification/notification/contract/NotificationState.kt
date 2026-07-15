package com.makeus.mody.feature.notification.notification.contract

import com.makeus.mody.core.commonui.base.UiState

data class NotificationState(
    // TODO(notification): 알림 목록 시안 확정 후 필드 정의 (목록/로딩/빈 상태)
    val isLoading: Boolean = false,
) : UiState
