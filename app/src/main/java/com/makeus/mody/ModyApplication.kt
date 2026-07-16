package com.makeus.mody

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.makeus.mody.core.commonui.activity.CurrentActivityHolder
import com.makeus.mody.notification.ModyNotificationChannel
import com.makeus.mody.notification.PushTokenRegistrar
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ModyApplication : Application() {

    @Inject lateinit var activityHolder: CurrentActivityHolder
    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        registerActivityLifecycleCallbacks(activityHolder)
        // 알림 채널 생성(8+ 필수) + 로그인 상태면 FCM 토큰 서버 동기화.
        ModyNotificationChannel.ensure(this)
        pushTokenRegistrar.sync()
    }
}
