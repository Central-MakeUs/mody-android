package com.makeus.mody.core.domain.notification

import com.makeus.mody.core.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 안 읽은 알림 존재 여부(상단바 알림 아이콘 뱃지).
 *
 * 뱃지는 피드/챌린지/마이 세 탭이 각자 그리지만 값은 하나여야 하고,
 * 알림함을 읽고 돌아오면 즉시 꺼져야 하며, 앱이 떠 있는 동안 푸시가 오면 켜져야 한다.
 * 화면마다 따로 조회하면 세 탭의 표시가 어긋나므로 단일 소스로 모은다.
 *
 * [refresh] 는 서버가 진실. 실패하면 직전 값을 유지한다(뱃지가 깜빡이는 것보다 낫다).
 */
@Singleton
class UnreadNotificationStore @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    /** 서버에서 다시 조회. 화면 진입 시점마다 호출. */
    suspend fun refresh() {
        try {
            _hasUnread.value = notificationRepository.hasUnreadNotifications()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 조회 실패는 무시 — 뱃지는 부가 정보라 직전 값을 유지한다.
        }
    }

    /** 푸시 수신 즉시 켜기(서버 재조회 없이). */
    fun markUnread() {
        _hasUnread.value = true
    }
}
