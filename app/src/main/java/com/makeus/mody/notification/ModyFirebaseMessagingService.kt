package com.makeus.mody.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.makeus.mody.R
import com.makeus.mody.core.domain.notification.NotificationDeepLink
import com.makeus.mody.core.domain.repository.PushTokenRepository
import com.makeus.mody.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FCM 수신 진입점.
 *  - onNewToken: 토큰 생성/갱신 → 서버 등록.
 *  - onMessageReceived: 앱이 포그라운드거나 data-only 메시지일 때 호출 → 시스템 알림 표시.
 *    (notification 페이로드는 백그라운드에선 시스템이 자동 표시하나, 딥링크 extra 를 싣기 위해
 *     여기서 직접 만든다.)
 */
@AndroidEntryPoint
class ModyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushTokenRepository: PushTokenRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        // 로그인 안 됐으면 등록 API 가 401 → runCatching 으로 무음 무시.
        // (로그인 후 앱 시작 시 PushTokenRegistrar.sync 가 다시 등록)
        scope.launch { runCatching { pushTokenRepository.register(token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: DEFAULT_TITLE
        val body = message.notification?.body ?: data["body"].orEmpty()

        showNotification(title, body, data)
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
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
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
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private companion object {
        const val DEFAULT_TITLE = "모디"
        const val REQUEST_CODE = 0
    }
}
