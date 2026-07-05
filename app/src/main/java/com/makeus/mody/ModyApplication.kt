package com.makeus.mody

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.makeus.mody.core.commonui.activity.CurrentActivityHolder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ModyApplication : Application() {

    @Inject lateinit var activityHolder: CurrentActivityHolder

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        registerActivityLifecycleCallbacks(activityHolder)
    }
}
