package com.makeus.mody.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.model.RemoteNotice
import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.domain.session.SessionExpiredNotifier
import com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.core.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * 앱 시작 시 진입 목적지 결정.
 * 세션 상태(로그인/온보딩)를 읽어 도메인 결과를 네비게이션 Route 로 매핑.
 * startRoute == null 이면 아직 판정 중(스플래시).
 * 세션 완전 만료(재발급/무음 재로그인 실패) 이벤트를 받으면 로그인 화면으로 보낸다.
 */
/**
 * 스플래시 진입 게이트 상태. iOS 와 동일 순서로 검사:
 * force_update_flag → minimum_supported_version → notice_flag → 통과.
 */
sealed interface SplashGateState {
    /** Remote Config 확인 중(스플래시 유지). */
    data object Checking : SplashGateState

    /** 강제 업데이트 또는 최소 버전 미달 — 스토어 이동 외 진행 불가. */
    data class UpdateRequired(val storeUrl: String?) : SplashGateState

    /** 공지 노출. skipPossible=false 면 확인 비활성(진행 차단). */
    data class Notice(val notice: RemoteNotice) : SplashGateState

    /** 게이트 통과 — 정상 라우팅 진행. */
    data object Passed : SplashGateState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val resolveStartDestination: ResolveStartDestinationUseCase,
    private val sessionExpiredNotifier: SessionExpiredNotifier,
    private val sessionRepository: SessionRepository,
    private val navigationHelper: NavigationHelper,
    private val remoteConfigRepository: RemoteConfigRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private companion object {
        const val TAG = "MainViewModel"

        // fetch 가 오래 걸려도 스플래시에 갇히지 않게 상한. 초과 시 캐시/기본값으로 평가(fail-open).
        const val REMOTE_CONFIG_TIMEOUT_MS = 5_000L
    }

    private val _startRoute = MutableStateFlow<Route?>(null)
    val startRoute = _startRoute.asStateFlow()

    private val _splashGate = MutableStateFlow<SplashGateState>(SplashGateState.Checking)
    val splashGate = _splashGate.asStateFlow()

    /** 진행 가능 공지(skipPossible=true)에서 확인 → 게이트 통과. */
    fun confirmNotice() {
        val gate = _splashGate.value
        if (gate is SplashGateState.Notice && gate.notice.skipPossible) {
            _splashGate.value = SplashGateState.Passed
        }
    }

    private fun checkSplashGate() = viewModelScope.launch {
        // 실패/타임아웃이어도 마지막 활성값(캐시)·기본값으로 평가 — 게이트가 앱 진입을 영구 차단하면 안 됨.
        withTimeoutOrNull(REMOTE_CONFIG_TIMEOUT_MS) {
            runCatching { remoteConfigRepository.refresh() }
        }
        val gate = remoteConfigRepository.splashGate()
        val notice = gate.notice
        _splashGate.value = when {
            gate.forceUpdate -> SplashGateState.UpdateRequired(gate.appStoreUrl)
            isBelowMinimum(currentVersionName(), gate.minimumSupportedVersion) ->
                SplashGateState.UpdateRequired(gate.appStoreUrl)
            notice != null -> SplashGateState.Notice(notice)
            else -> SplashGateState.Passed
        }
    }

    private fun currentVersionName(): String? = runCatching {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
    }.getOrNull()

    /** semver 비교("1.2.3"). 파싱 불가/미지정이면 차단하지 않음. */
    private fun isBelowMinimum(current: String?, minimum: String?): Boolean {
        val cur = current?.toVersionParts() ?: return false
        val min = minimum?.toVersionParts() ?: return false
        for (i in 0 until maxOf(cur.size, min.size)) {
            val a = cur.getOrElse(i) { 0 }
            val b = min.getOrElse(i) { 0 }
            if (a != b) return a < b
        }
        return false
    }

    // "1.0.0-local" → [1,0,0]. 숫자가 아니면 null(검사 생략).
    private fun String.toVersionParts(): List<Int>? =
        substringBefore('-').split('.').map { it.toIntOrNull() ?: return null }

    init {
        checkSplashGate()
        viewModelScope.launch {
            // 세션 조회 실패(DataStore/토큰) 시 스플래시에 갇히지 않도록 로그인으로 폴백
            val destination = runCatching { resolveStartDestination() }
                .getOrDefault(StartDestination.AUTH)
            _startRoute.value = when (destination) {
                StartDestination.AUTH -> AuthGraphBaseRoute
                StartDestination.ONBOARDING -> OnboardingGraphBaseRoute
                StartDestination.GROUP -> GroupGraphBaseRoute
                StartDestination.MAIN -> MainRoute
            }
        }
        viewModelScope.launch {
            sessionExpiredNotifier.events.collect {
                runCatching { sessionRepository.clear() }
                val sent = navigationHelper.navigate(
                    NavigationEvent.To(AuthGraphBaseRoute, popUpTo = true),
                )
                if (!sent) {
                    Log.w(TAG, "Dropped session-expired navigation event")
                }
            }
        }
    }
}
