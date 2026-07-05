package com.makeus.mody.core.commonui.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 현재 포그라운드 Activity 를 추적한다.
 * Activity Context 가 필요한 SDK 호출(카카오 로그인 등)을 ViewModel/Repository 에서 쓰기 위함.
 * Application.onCreate 에서 [registerActivityLifecycleCallbacks] 로 등록.
 */
@Singleton
class CurrentActivityHolder @Inject constructor() : Application.ActivityLifecycleCallbacks {

    var current: Activity? = null
        private set

    override fun onActivityResumed(activity: Activity) {
        current = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (current === activity) current = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (current === activity) current = null
    }
}
