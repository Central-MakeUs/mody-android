package com.makeus.mody.core.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.makeus.mody.core.domain.error.ErrorReporter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crashlytics non-fatal 보고.
 *
 * 수집 on/off 는 여기서 보지 않는다 — 앱 시작 시 [FirebaseCrashlytics.setCrashlyticsCollectionEnabled]
 * 로 정해지고, 꺼져 있으면 아래 호출들은 그냥 버려진다(debug 빌드).
 */
@Singleton
class CrashlyticsErrorReporter @Inject constructor() : ErrorReporter {

    private val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics.getInstance()

    override fun report(throwable: Throwable, context: Map<String, String>) {
        // custom key 는 "직전에 설정된 값"이 다음 리포트에 붙는 전역 상태다. 리포트마다
        // 남기고 싶은 값이 달라 로그 라인으로도 함께 남긴다(키가 다음 건에 새더라도 추적 가능).
        if (context.isNotEmpty()) {
            context.forEach { (key, value) -> crashlytics.setCustomKey(key, value) }
            crashlytics.log(context.entries.joinToString(" ") { "${it.key}=${it.value}" })
        }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }
}
