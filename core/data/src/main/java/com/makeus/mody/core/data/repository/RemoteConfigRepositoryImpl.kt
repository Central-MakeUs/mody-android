package com.makeus.mody.core.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.Firebase
import com.makeus.mody.core.domain.model.RemoteNotice
import com.makeus.mody.core.domain.model.SplashGate
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RemoteConfigRepositoryImpl @Inject constructor() : RemoteConfigRepository {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig.apply {
        setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL },
        )
        // 원격값 없거나 fetch 전 기본값 — 심사 안전 방향(챌린지 숨김·게이트 미발동).
        setDefaultsAsync(
            mapOf(
                KEY_IS_PHASE_ONE to true,
                KEY_FORCE_UPDATE to false,
                KEY_MIN_SUPPORTED_VERSION to "",
                KEY_APP_STORE_URL to "",
                KEY_NOTICE to "",
            ),
        )
    }

    private val _challengeEnabled = MutableStateFlow(readChallengeEnabled())
    override val challengeEnabled: StateFlow<Boolean> = _challengeEnabled.asStateFlow()

    override suspend fun refresh() {
        suspendCancellableCoroutine { cont ->
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task -> cont.resume(task.isSuccessful) }
        }
        // 성공/실패 무관하게 현재 활성값 반영(실패 시 캐시/기본값).
        _challengeEnabled.value = readChallengeEnabled()
    }

    // iOS 와 공유하는 콘솔 파라미터(플랫폼 조건 분리됨): is_phase_one_flag.
    // Phase 1 = 챌린지 미노출 단계 → 챌린지 노출은 그 부정.
    private fun readChallengeEnabled(): Boolean = !remoteConfig.getBoolean(KEY_IS_PHASE_ONE)

    override fun splashGate(): SplashGate = SplashGate(
        forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
        minimumSupportedVersion = remoteConfig.getString(KEY_MIN_SUPPORTED_VERSION).ifBlank { null },
        appStoreUrl = remoteConfig.getString(KEY_APP_STORE_URL).ifBlank { null },
        notice = parseNotice(remoteConfig.getString(KEY_NOTICE)),
    )

    /**
     * notice_flag JSON → [RemoteNotice]. 비었거나 파싱 실패면 공지 없음(null).
     * 필드 명은 iOS 정의를 방어적으로 수용: message|body|content.
     */
    private fun parseNotice(raw: String): RemoteNotice? {
        if (raw.isBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            val title = json.optString("title")
            val message = json.optString("message")
                .ifBlank { json.optString("body") }
                .ifBlank { json.optString("content") }
            if (title.isBlank() && message.isBlank()) return null
            RemoteNotice(
                title = title.ifBlank { "공지사항" },
                message = message,
                skipPossible = json.optBoolean("skipPossible", true),
            )
        }.getOrNull()
    }

    private companion object {
        /** [Phase 1.0] Visible 플래그 — true 면 Phase 1(챌린지 숨김). iOS/Android 공용 키. */
        const val KEY_IS_PHASE_ONE = "is_phase_one_flag"
        const val KEY_FORCE_UPDATE = "force_update_flag"
        const val KEY_MIN_SUPPORTED_VERSION = "minimum_supported_version"
        const val KEY_APP_STORE_URL = "app_store_url"
        const val KEY_NOTICE = "notice_flag"

        // 개발 단계: 매 실행 즉시 fetch 로 플래그 토글 확인 용이. 배포 시 3600 등으로 상향 권장.
        const val MIN_FETCH_INTERVAL = 0L
    }
}
