package com.makeus.mody.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.makeus.mody.R
import com.makeus.mody.core.domain.notification.NotificationDeepLink
import com.makeus.mody.core.domain.notification.PushTokenSynchronizer
import com.makeus.mody.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM 수신 진입점.
 *  - onNewToken: 토큰 생성/갱신 → 서버 재동기화(로그인 상태에서만).
 *  - onMessageReceived: 앱이 포그라운드거나 data-only 메시지일 때 호출 → 시스템 알림 표시.
 *    (notification 페이로드는 백그라운드에선 시스템이 자동 표시하나, 딥링크 extra 를 싣기 위해
 *     여기서 직접 만든다.)
 */
@AndroidEntryPoint
class ModyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushTokenSynchronizer: PushTokenSynchronizer

    override fun onNewToken(token: String) {
        // 토큰 갱신 → 현재 토큰 재동기화. sync 가 로그인 여부 판정 + 자체 스코프로 fire-and-forget.
        // (서비스에 별도 스코프를 두면 서비스 파괴 시 미취소 코루틴이 남아 sync 로 위임한다.)
        pushTokenSynchronizer.sync()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = message.notification?.title ?: data["title"]
        val body = message.notification?.body ?: data["body"]

        // 표시할 제목/본문이 전혀 없으면 무음(data-only) 메시지 → 스퓨리어스 빈 알림 방지.
        if (title == null && body == null) return
        showNotification(title ?: DEFAULT_TITLE, body.orEmpty(), data)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // 딥링크 정보 전달 → MainActivity 가 extra 를 뽑아 라우팅.
            data[NotificationDeepLink.KEY_TYPE]?.let {
                putExtra(NotificationDeepLink.KEY_TYPE, it)
            }
            data[NotificationDeepLink.KEY_TARGET_ID]?.let {
                putExtra(NotificationDeepLink.KEY_TARGET_ID, it)
            }
        }
        // 알림마다 고유 id → PendingIntent request code + notify 에 동일 사용.
        // 고정 code + FLAG_UPDATE_CURRENT 면 새 알림이 기존 알림 extra(딥링크)를 덮어씀.
        val notificationId = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, ModyNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(this)
        // 13+ POST_NOTIFICATIONS 미허용이면 notify 가 무시됨(크래시 아님). 표시 안 될 뿐.
        if (manager.areNotificationsEnabled()) {
            manager.notify(notificationId, notification)
        }
    }

    private companion object {
        const val DEFAULT_TITLE = "모디"
    }
}
