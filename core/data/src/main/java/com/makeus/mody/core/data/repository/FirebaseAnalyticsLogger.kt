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

    override fun logScreenView(screenName: String, params: Map<String, String>) {
        val name = screenName.take(PARAM_VALUE_MAX)
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
                // 자동 수집되는 screen_class 는 이 앱에선 전부 MainActivity 라 구분에 못 쓴다.
                // 화면 이름을 같이 넣어 어느 축으로 봐도 같은 결과가 나오게 한다.
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, name)
                // screen_name·screen_class 두 자리를 뺀 나머지만 받는다.
                putParams(params, EVENT_PARAM_MAX - 2)
            },
        )
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(
            name.take(EVENT_NAME_MAX),
            Bundle().apply { putParams(params, EVENT_PARAM_MAX) },
        )
    }

    override fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(
            name.take(USER_PROPERTY_NAME_MAX),
            value?.take(USER_PROPERTY_VALUE_MAX),
        )
    }

    /**
     * 25개를 넘기면 초과분을 GA4 가 말없이 버린다. 어느 게 잘렸는지 알 수 없어
     * 여기서 먼저 자른다(Map 순회 순서 = 호출부가 넣은 순서).
     */
    private fun Bundle.putParams(params: Map<String, String>, max: Int) {
        params.entries.take(max).forEach { (key, value) ->
            putString(key.take(PARAM_KEY_MAX), value.take(PARAM_VALUE_MAX))
        }
    }

    private companion object {
        // GA4 제한. 넘기면 이벤트가 통째로 버려져서 잘라 보낸다.
        const val EVENT_NAME_MAX = 40
        const val PARAM_KEY_MAX = 40
        const val PARAM_VALUE_MAX = 100
        const val EVENT_PARAM_MAX = 25
        const val USER_PROPERTY_NAME_MAX = 24
        const val USER_PROPERTY_VALUE_MAX = 36
    }
}
