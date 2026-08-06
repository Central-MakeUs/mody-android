package com.makeus.mody.presentation

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.domain.model.RemoteNotice
import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.domain.session.SessionExpiredNotifier
import com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase
import com.makeus.mody.core.domain.usecase.SyncTodayStepsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.core.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val syncTodaySteps: SyncTodayStepsUseCase,
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

        // onStart 는 잠깐 다른 앱에 갔다 와도 불린다. 연타 수준의 재진입까지 서버에 올리지 않도록.
        const val STEP_SYNC_MIN_INTERVAL_MS = 60_000L
    }

    /** 마지막 걸음 수 동기화 시각(부팅 기준). 0 이면 아직 안 함. */
    private var lastStepSyncAt = 0L

    /** 진행 중인 걸음 수 동기화. 겹쳐 돌면 늦게 끝난 오래된 값이 최신 기록을 덮어쓴다. */
    private var stepSyncJob: Job? = null

    private val _startRoute = MutableStateFlow<Route?>(null)
    val startRoute = _startRoute.asStateFlow()

    private val _splashGate = MutableStateFlow<SplashGateState>(SplashGateState.Checking)
    val splashGate = _splashGate.asStateFlow()

    /**
     * 앱 진입(첫 실행 + 백그라운드 복귀)마다 오늘 걸음 수를 서버에 반영.
     *
     * 챌린지 탭에 들어가야만 올라가던 것을 메운다. 권한이 없으면 use case 가 조용히 건너뛰므로
     * 여기서 팝업이 뜨는 일은 없다. 실패는 다음 진입에 다시 시도하면 되므로 로그만 남긴다.
     */
    fun onAppEntered() {
        val now = SystemClock.elapsedRealtime()
        if (lastStepSyncAt != 0L && now - lastStepSyncAt < STEP_SYNC_MIN_INTERVAL_MS) return
        // 최소 간격만으로는 부족하다 — 앞선 동기화가 느리면 두 번이 겹치고, 먼저 시작한 쪽이
        // 늦게 끝나면서 같은 날짜에 더 적은 걸음 수를 덮어쓴다.
        if (stepSyncJob?.isActive == true) return
        lastStepSyncAt = now
        stepSyncJob = viewModelScope.launch {
            try {
                syncTodaySteps()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "앱 진입 걸음 수 동기화 실패", e)
            }
        }
    }

    /** 진행 가능 공지(skipPossible=true)에서 확인 → 게이트 통과. */
    fun confirmNotice() {
        val gate = _splashGate.value
        if (gate is SplashGateState.Notice && gate.notice.skipPossible) {
            _splashGate.value = SplashGateState.Passed
        }
    }

    private fun checkSplashGate() = viewModelScope.launch {
        // 실패/타임아웃이어도 마지막 활성값(캐시)·기본값으로 평가 — 게이트가 앱 진입을 영구 차단하면 안 됨.
        // 취소(타임아웃 포함)는 삼키지 않고 전파해야 withTimeoutOrNull 이 정상 동작한다.
        withTimeoutOrNull(REMOTE_CONFIG_TIMEOUT_MS) {
            try {
                remoteConfigRepository.refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // fetch 실패 → 캐시/기본값으로 평가
            }
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
