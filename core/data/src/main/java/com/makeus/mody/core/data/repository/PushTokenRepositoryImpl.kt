package com.makeus.mody.core.data.repository

import android.content.Context
import android.provider.Settings
import com.makeus.mody.core.domain.repository.PushTokenRepository
import com.makeus.mody.core.network.api.NotificationApi
import com.makeus.mody.core.network.model.notification.PushTokenDisableRequest
import com.makeus.mody.core.network.model.notification.PushTokenRegisterRequest
import com.makeus.mody.core.network.model.unwrapResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationApi: NotificationApi,
) : PushTokenRepository {

    // 기기+앱서명당 안정적인 식별자. 퍼미션 불필요. 서버가 기기 단위 토큰 관리에 사용.
    private val deviceId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .orEmpty()

    override suspend fun register(fcmToken: String) {
        notificationApi.registerPushToken(
            PushTokenRegisterRequest(
                deviceId = deviceId,
                platform = PLATFORM_ANDROID,
                fcmToken = fcmToken,
            ),
        ).unwrapResult()
    }

    override suspend fun unregister() {
        notificationApi.disablePushToken(
            PushTokenDisableRequest(deviceId = deviceId),
        ).unwrapResult()
    }

    private companion object {
        const val PLATFORM_ANDROID = "ANDROID"
    }
}
