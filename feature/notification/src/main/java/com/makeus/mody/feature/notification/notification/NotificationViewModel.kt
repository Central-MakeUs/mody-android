package com.makeus.mody.feature.notification.notification

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.domain.model.Notification
import com.makeus.mody.core.domain.model.NotificationType
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.domain.notification.UnreadNotificationStore
import com.makeus.mody.core.domain.repository.NotificationRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationDestination
import com.makeus.mody.core.navigation.NotificationLinkParser
import com.makeus.mody.feature.notification.notification.contract.NotificationIntent
import com.makeus.mody.feature.notification.notification.contract.NotificationState
import com.makeus.mody.feature.notification.notification.contract.NotificationUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val navigationHelper: NavigationHelper,
    private val pendingGroupSelectionHolder: PendingGroupSelectionHolder,
    private val unreadNotificationStore: UnreadNotificationStore,
    private val remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<NotificationState, NotificationIntent>(NotificationState()) {

    /** 다음 페이지 커서. null 이면 첫 페이지이거나 더 없음. */
    private var nextCursor: Long? = null

    /** 서버에 더 받을 페이지가 있는지. */
    private var hasNext: Boolean = true

    init {
        loadNotifications(initial = true)
    }

    /**
     * 댓글 기능이 닫혀 있으면 댓글 알림은 목록에서 뺀다.
     *
     * 읽음 처리는 거르기 전 원본으로 하므로(아래 [markUnreadAsRead]) 숨긴 알림 때문에
     * 뱃지가 남지는 않는다. 서버가 계속 보내는 걸 클라가 가리는 형태다.
     */
    private fun List<Notification>.filterByFeatureFlags(): List<Notification> =
        if (remoteConfigRepository.commentEnabled.value) {
            this
        } else {
            filterNot { it.type == NotificationType.COMMENT || it.type == NotificationType.COMMENT_CREATED }
        }

    override suspend fun processIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
            is NotificationIntent.LoadMore -> loadNotifications(initial = false)
            is NotificationIntent.ItemClicked -> navigateByLink(intent.link)
        }
    }

    /** 알림 link 파싱 → 이동. 미지원/파싱 실패 링크는 무시. */
    private fun navigateByLink(link: String?) {
        when (val dest = NotificationLinkParser.parse(link)) {
            // 알림 목록을 스택에서 빼고 대상 화면으로 치환 — 항목을 눌러 이동한 뒤
            // 뒤로가기를 하면 이미 확인한 목록이 아니라 Main 으로 돌아간다.
            // 작성 "완료"는 기록 VM 이 BackTo(MainRoute)로 피드까지 pop 한다.
            is NotificationDestination.Screen ->
                navigationHelper.navigate(NavigationEvent.Replace(dest.route))
            // 그룹홈은 별도 라우트가 없어 Feed 탭 + 그룹 전환으로. holder 에 groupId 보관 후
            // 알림 목록을 pop(Up)해 Main 으로 복귀 → MainScreenViewModel/FeedViewModel 이 반응.
            is NotificationDestination.GroupHome -> {
                pendingGroupSelectionHolder.set(dest.groupId)
                navigationHelper.navigate(NavigationEvent.Up)
            }
            null -> Unit
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
                val newItems = page.notifications.filterByFeatureFlags().map { it.toUiModel(now) }
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
        val unread = notifications.filterNot { it.isRead }
        if (unread.isEmpty()) return
        viewModelScope.launch {
            // 실패는 건별로 흡수(하나 실패해도 나머지는 읽음 처리).
            coroutineScope {
                unread.map { notification ->
                    async {
                        runCatching {
                            notificationRepository.readNotification(notification.notificationId)
                        }
                    }
                }.awaitAll()
            }
            // 상단바 뱃지 갱신. 아직 안 불러온 페이지에 미확인이 남아 있을 수 있어
            // 무조건 끄지 않고 서버에 다시 묻는다.
            unreadNotificationStore.refresh()
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
        link = link,
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
