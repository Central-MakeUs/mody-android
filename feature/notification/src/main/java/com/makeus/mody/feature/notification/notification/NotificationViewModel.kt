package com.makeus.mody.feature.notification.notification

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.domain.model.Notification
import com.makeus.mody.core.domain.model.NotificationType
import com.makeus.mody.core.domain.repository.NotificationRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.notification.notification.contract.NotificationIntent
import com.makeus.mody.feature.notification.notification.contract.NotificationState
import com.makeus.mody.feature.notification.notification.contract.NotificationUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<NotificationState, NotificationIntent>(NotificationState()) {

    /** 다음 페이지 커서. null 이면 첫 페이지이거나 더 없음. */
    private var nextCursor: Long? = null

    /** 서버에 더 받을 페이지가 있는지. */
    private var hasNext: Boolean = true

    init {
        loadNotifications(initial = true)
    }

    override suspend fun processIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
            is NotificationIntent.LoadMore -> loadNotifications(initial = false)
        }
    }

    private fun loadNotifications(initial: Boolean) {
        // 로딩 중이거나(중복 요청) 더 받을 게 없으면 무시. 첫 로드는 항상 진행.
        if (state.value.isLoading) return
        if (!initial && !hasNext) return

        viewModelScope.launch {
            setState { copy(isLoading = true) }
            runCatching {
                notificationRepository.getNotifications(cursor = nextCursor)
            }.onSuccess { page ->
                nextCursor = page.nextCursor
                hasNext = page.hasNext
                val now = Instant.now()
                val newItems = page.notifications.map { it.toUiModel(now) }
                setState {
                    copy(
                        notifications = if (initial) newItems else notifications + newItems,
                        isLoading = false,
                        isInitialLoaded = true,
                    )
                }
                // 진입만 해도 확인 처리(시안 정책): 이번에 받은 미확인 알림을 서버에 읽음 처리.
                // 화면 배경은 로드 시점 상태(하이라이트)를 유지하고, 다음 진입부터 확인됨으로 표시된다.
                markUnreadAsRead(page.notifications)
            }.onFailure {
                setState { copy(isLoading = false, isInitialLoaded = true) }
            }
        }
    }

    private fun markUnreadAsRead(notifications: List<Notification>) {
        notifications.filterNot { it.isRead }.forEach { notification ->
            viewModelScope.launch {
                runCatching { notificationRepository.readNotification(notification.notificationId) }
            }
        }
    }
}

private fun Notification.toUiModel(now: Instant): NotificationUiModel =
    NotificationUiModel(
        id = notificationId,
        iconRes = type.iconRes(),
        title = title,
        description = description,
        timeText = formatRelativeTime(createdAt, now),
        isRead = isRead,
    )

private fun NotificationType.iconRes(): Int = when (this) {
    NotificationType.GROUP_JOINED,
    NotificationType.GROUP_MEMBER_JOINED -> R.drawable.ic_party

    NotificationType.EXERCISE_REMINDER -> R.drawable.ic_exercise
    NotificationType.MEAL_REMINDER -> R.drawable.ic_cook

    NotificationType.COMMENT,
    NotificationType.COMMENT_CREATED -> R.drawable.ic_comment

    NotificationType.STREAK,
    NotificationType.GROUP_RECORD_STREAK_RISK -> R.drawable.ic_fire

    NotificationType.NUDGE,
    NotificationType.BUDDY_NUDGE -> R.drawable.ic_nudge

    NotificationType.STEP_CHALLENGE_COMPLETED -> R.drawable.ic_footprint

    NotificationType.CHALLENGE,
    NotificationType.WEEKLY_CHALLENGE_COMPLETED -> R.drawable.ic_award

    NotificationType.RECORD_REMINDER,
    NotificationType.DEV_TEST,
    NotificationType.UNKNOWN -> R.drawable.ic_alarm
}

/** "방금 전 / N분 전 / N시간 전 / N일 전", 일주일 넘으면 "M월 D일". */
private fun formatRelativeTime(createdAt: Instant, now: Instant): String {
    val seconds = Duration.between(createdAt, now).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "방금 전"
        seconds < 3600 -> "${seconds / 60}분 전"
        seconds < 86_400 -> "${seconds / 3600}시간 전"
        seconds < 604_800 -> "${seconds / 86_400}일 전"
        else -> createdAt.atZone(ZoneId.systemDefault()).let { "${it.monthValue}월 ${it.dayOfMonth}일" }
    }
}
