package com.makeus.mody.core.domain.model

import java.time.Instant

/** 인박스 알림 종류. 서버 enum 매핑, 모르는 값은 UNKNOWN. 화면 아이콘/이동 분기 키. */
enum class NotificationType {
    GROUP_JOINED,
    RECORD_REMINDER,
    COMMENT,
    STREAK,
    NUDGE,
    CHALLENGE,
    GROUP_MEMBER_JOINED,
    EXERCISE_REMINDER,
    MEAL_REMINDER,
    COMMENT_CREATED,
    GROUP_RECORD_STREAK_RISK,
    BUDDY_NUDGE,
    STEP_CHALLENGE_COMPLETED,
    WEEKLY_CHALLENGE_COMPLETED,
    DEV_TEST,
    UNKNOWN,
}

/** 인박스 알림 한 건. */
data class Notification(
    val notificationId: Long,
    val type: NotificationType,
    val title: String,
    val description: String,
    val createdAt: Instant,
    val isRead: Boolean,
)

/** 커서 페이지네이션 결과. */
data class NotificationPage(
    val notifications: List<Notification>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
