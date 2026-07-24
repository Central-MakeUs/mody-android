package com.makeus.mody.feature.mypage

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.navigation.MyPageGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.feature.mypage.contract.MyPageIntent
import com.makeus.mody.feature.mypage.contract.MyPageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<MyPageState, MyPageIntent>(MyPageState()) {

    init {
        load()
    }

    override suspend fun processIntent(intent: MyPageIntent) {
        when (intent) {
            is MyPageIntent.Refresh -> load()
            is MyPageIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))

            is MyPageIntent.ProfileSettingClicked ->
                navigationHelper.navigate(NavigationEvent.To(MyPageGraph.ProfileEditRoute))

            is MyPageIntent.NotificationSettingClicked ->
                navigationHelper.navigate(NavigationEvent.To(MyPageGraph.NotificationSettingRoute))

            is MyPageIntent.GroupSettingClicked ->
                navigationHelper.navigate(NavigationEvent.To(MyPageGraph.GroupSettingRoute))

            is MyPageIntent.WeightRecordClicked -> setState { copy(showWeightSheet = true) }
            is MyPageIntent.WeightRecordDismissed -> setState { copy(showWeightSheet = false) }
            is MyPageIntent.WeightRecordSubmitted -> recordWeight(intent.recordedOn, intent.weightKg)
            is MyPageIntent.WeightErrorShown -> setState { copy(weightError = null) }

            // TODO(mypage): 서브 화면 구현 후 라우팅 연결.
            is MyPageIntent.HealthDataSettingClicked -> Unit
        }
    }

    private fun recordWeight(recordedOn: String, weightKg: Double) = viewModelScope.launch {
        if (currentState.isRecordingWeight) return@launch
        setState { copy(isRecordingWeight = true, weightError = null) }
        try {
            myPageRepository.recordWeight(recordedOn, weightKg)
            // 저장 성공 → 요약 갱신(실패해도 기존 값 유지). 시트 닫기.
            val w = weightSummaryOrNull()
            setState { copy(weight = w ?: weight, isRecordingWeight = false, showWeightSheet = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 저장 실패 → 시트 유지 + 에러 노출(입력값 보존, 재시도 가능).
            setState { copy(isRecordingWeight = false, weightError = "저장에 실패했어요. 다시 시도해주세요.") }
        }
    }

    /** 취소는 전파, 그 외 실패만 null. (runCatching은 CancellationException까지 삼켜 사용 금지) */
    private suspend fun weightSummaryOrNull() = try {
        myPageRepository.getWeightSummary()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    /** 프로필 로드 실패도 null 로 흡수. (weightSummaryOrNull 과 동일 정책) */
    private suspend fun profileOrNull() = try {
        myPageRepository.getProfile()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        // async 를 launch 의 직접 자식으로 두고 예외가 나면 try/catch 를 우회해
        // 부모 Job 으로 전파되어 크래시난다(예: 토큰 만료 시 getProfile 401).
        // 각 호출을 개별적으로 잡아 null 로 흡수한 뒤 supervisorScope 로 병렬 실행한다.
        val (p, w) = supervisorScope {
            val profileDeferred = async { profileOrNull() }
            val weightDeferred = async { weightSummaryOrNull() }
            profileDeferred.await() to weightDeferred.await()
        }
        // 실패한 값은 기존 상태 유지. 로딩만 해제.
        setState {
            copy(
                nickname = p?.nickname ?: nickname,
                profileImageUrl = p?.profileImageUrl ?: profileImageUrl,
                daysTogether = p?.daysTogether ?: daysTogether,
                weight = w ?: weight,
                isLoading = false,
            )
        }
    }
}
