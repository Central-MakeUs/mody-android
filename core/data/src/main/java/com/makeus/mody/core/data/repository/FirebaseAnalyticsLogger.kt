package com.makeus.mody.core.data.repository

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsLogger @Inject constructor(
    @ApplicationContext context: Context,
) : AnalyticsLogger {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logScreenView(screenName: String) {
        val name = screenName.take(PARAM_VALUE_MAX)
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
                // 자동 수집되는 screen_class 는 이 앱에선 전부 MainActivity 라 구분에 못 쓴다.
                // 화면 이름을 같이 넣어 어느 축으로 봐도 같은 결과가 나오게 한다.
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, name)
            },
        )
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(
            name.take(EVENT_NAME_MAX),
            Bundle().apply {
                params.forEach { (key, value) ->
                    putString(key.take(PARAM_KEY_MAX), value.take(PARAM_VALUE_MAX))
                }
            },
        )
    }

    private companion object {
        // GA4 제한. 넘기면 이벤트가 통째로 버려져서 잘라 보낸다.
        const val EVENT_NAME_MAX = 40
        const val PARAM_KEY_MAX = 40
        const val PARAM_VALUE_MAX = 100
    }
}
