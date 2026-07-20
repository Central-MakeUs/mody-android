package com.makeus.mody.core.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.Firebase
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
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
        // 원격값 없거나 fetch 전이면 이 기본값 사용(챌린지 숨김).
        setDefaultsAsync(mapOf(KEY_CHALLENGE_ENABLED to false))
    }

    private val _challengeEnabled = MutableStateFlow(remoteConfig.getBoolean(KEY_CHALLENGE_ENABLED))
    override val challengeEnabled: StateFlow<Boolean> = _challengeEnabled.asStateFlow()

    override suspend fun refresh() {
        val activated = suspendCancellableCoroutine { cont ->
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task -> cont.resume(task.isSuccessful) }
        }
        // 성공/실패 무관하게 현재 활성값 반영(실패 시 캐시/기본값).
        _challengeEnabled.value = remoteConfig.getBoolean(KEY_CHALLENGE_ENABLED)
        // activated 로그 필요 시 추가. (여기선 값만 갱신)
    }

    private companion object {
        const val KEY_CHALLENGE_ENABLED = "challenge_tab_enabled"

        // 개발 단계: 매 실행 즉시 fetch 로 플래그 토글 확인 용이. 배포 시 3600 등으로 상향 권장.
        const val MIN_FETCH_INTERVAL = 0L
    }
}
