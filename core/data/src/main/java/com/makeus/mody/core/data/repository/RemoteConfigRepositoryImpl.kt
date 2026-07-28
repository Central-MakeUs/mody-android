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
                KEY_GUEST_LOGIN to false,
                KEY_FORCE_UPDATE to false,
                KEY_MIN_SUPPORTED_VERSION to "",
                KEY_APP_STORE_URL to "",
                KEY_NOTICE to "",
                KEY_PRIVACY_POLICY_URL to DEFAULT_PRIVACY_POLICY_URL,
                KEY_TERMS_OF_SERVICE_URL to DEFAULT_TERMS_OF_SERVICE_URL,
            ),
        )
    }

    // 초기값은 하드코딩 안전 기본(Phase 2 숨김). 생성 시점에 getBoolean 을 읽으면
    // setDefaultsAsync 가 아직 적용 전이라 내장 기본(false)이 나와 !false = true 로
    // Phase 2 가 잠깐 노출될 수 있다 → 원격/캐시값은 refresh 이후에만 반영.
    private val _phaseTwoFeaturesEnabled = MutableStateFlow(false)
    override val phaseTwoFeaturesEnabled: StateFlow<Boolean> = _phaseTwoFeaturesEnabled.asStateFlow()

    // 히든 로그인도 같은 이유로 fetch 전엔 하드코딩 차단(false).
    private val _guestLoginEnabled = MutableStateFlow(false)
    override val guestLoginEnabled: StateFlow<Boolean> = _guestLoginEnabled.asStateFlow()

    override suspend fun refresh() {
        try {
            suspendCancellableCoroutine { cont ->
                remoteConfig.fetchAndActivate()
                    .addOnCompleteListener { task -> cont.resume(task.isSuccessful) }
            }
        } finally {
            // 성공/실패/타임아웃 취소 모두 현재 활성값(캐시·기본값)으로 상태 갱신.
            _phaseTwoFeaturesEnabled.value = readPhaseTwoEnabled()
            _guestLoginEnabled.value = remoteConfig.getBoolean(KEY_GUEST_LOGIN)
        }
    }

    // 약관 URL 은 플래그와 달리 fetch 전에도 기본값(setDefaultsAsync)이 즉시 반환되므로
    // refresh 없이 바로 사용 가능. 콘솔에 값이 있으면 그 값이, 없으면 GitHub Pages 기본값이 나온다.
    override fun privacyPolicyUrl(): String =
        remoteConfig.getString(KEY_PRIVACY_POLICY_URL).ifBlank { DEFAULT_PRIVACY_POLICY_URL }

    override fun termsOfServiceUrl(): String =
        remoteConfig.getString(KEY_TERMS_OF_SERVICE_URL).ifBlank { DEFAULT_TERMS_OF_SERVICE_URL }

    // iOS 와 공유하는 콘솔 파라미터(플랫폼 조건 분리됨): is_phase_one_flag.
    // Phase 1 = 챌린지 미노출 단계 → Phase 2 기능 노출은 그 부정.
    private fun readPhaseTwoEnabled(): Boolean = !remoteConfig.getBoolean(KEY_IS_PHASE_ONE)

    override fun splashGate(): SplashGate = SplashGate(
        forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
        minimumSupportedVersion = remoteConfig.getString(KEY_MIN_SUPPORTED_VERSION).ifBlank { null },
        appStoreUrl = remoteConfig.getString(KEY_APP_STORE_URL).ifBlank { null },
        notice = parseNotice(remoteConfig.getString(KEY_NOTICE)),
    )

    /**
     * notice_flag JSON → [RemoteNotice]. 비었거나 파싱 실패면 공지 없음(null).
     * iOS 정의 스키마: {"title","contents","skipPossible"} — 혹시 모를 변형(message)도 수용.
     */
    private fun parseNotice(raw: String): RemoteNotice? {
        if (raw.isBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            val title = json.optString("title")
            val message = json.optString("contents").ifBlank { json.optString("message") }
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

        /** 심사용 히든 로그인 허용 — 심사 기간에만 콘솔에서 true 게시. */
        const val KEY_GUEST_LOGIN = "guest_login_flag"
        const val KEY_FORCE_UPDATE = "force_update_flag"
        const val KEY_MIN_SUPPORTED_VERSION = "minimum_supported_version"
        const val KEY_APP_STORE_URL = "app_store_url"
        const val KEY_NOTICE = "notice_flag"

        /** 약관 상세 웹 URL. 콘솔 미설정 시 아래 기본값(GitHub Pages) 사용. */
        const val KEY_PRIVACY_POLICY_URL = "privacy_policy_url"
        const val KEY_TERMS_OF_SERVICE_URL = "terms_of_service_url"

        // GitHub Pages 게시본. 조직 계정 확보 시 RC 콘솔에서 덮어써 무중단 교체.
        const val DEFAULT_PRIVACY_POLICY_URL = "https://doyun-1999.github.io/mody-legal/privacy.html"
        const val DEFAULT_TERMS_OF_SERVICE_URL = "https://doyun-1999.github.io/mody-legal/terms.html"

        // 개발 단계: 매 실행 즉시 fetch 로 플래그 토글 확인 용이. 배포 시 3600 등으로 상향 권장.
        const val MIN_FETCH_INTERVAL = 0L
    }
}
