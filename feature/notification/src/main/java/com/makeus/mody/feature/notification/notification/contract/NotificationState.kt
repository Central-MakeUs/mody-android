package com.makeus.mody.feature.notification.notification.contract

import com.makeus.mody.core.commonui.base.UiState

data class NotificationState(
    val notifications: List<NotificationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    // 첫 페이지 응답을 받은 뒤에만 빈 상태를 판단(초기 진입 깜빡임 방지).
    val isInitialLoaded: Boolean = false,
) : UiState {
    val isEmpty: Boolean get() = isInitialLoaded && notifications.isEmpty()
}
