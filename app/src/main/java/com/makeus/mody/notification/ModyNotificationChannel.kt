package com.makeus.mody.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * 앱 기본 알림 채널. Android 8(O)+ 필수 — 채널 없으면 알림이 안 뜬다.
 * ModyApplication.onCreate 에서 1회 생성.
 */
object ModyNotificationChannel {
    const val ID = "mody_default"
    private const val NAME = "모디 알림"
    private const val DESCRIPTION = "기록 리마인더, 댓글, 챌린지 등 모디 알림"

    fun ensure(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        // 같은 id 로 재생성해도 idempotent(설정만 갱신).
        val channel = NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = DESCRIPTION
        }
        manager.createNotificationChannel(channel)
    }
}
