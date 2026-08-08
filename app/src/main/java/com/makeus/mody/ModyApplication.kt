package com.makeus.mody

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
        // 개발 중 크래시·non-fatal 이 대시보드를 덮지 않게 debug 는 수집 자체를 끈다.
        // (gradle 의 mappingFileUploadEnabled 와 별개 — 이건 런타임 전송 스위치다.)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        registerActivityLifecycleCallbacks(activityHolder)
        // 알림 채널 생성(8+ 필수) + 로그인 상태면 FCM 토큰 서버 동기화.
        ModyNotificationChannel.ensure(this)
        pushTokenRegistrar.sync()
    }
}
