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

            is MyPageIntent.WeightRecordClicked -> setState { copy(showWeightSheet = true) }
            is MyPageIntent.WeightRecordDismissed -> setState { copy(showWeightSheet = false) }
            is MyPageIntent.WeightRecordSubmitted -> recordWeight(intent.recordedOn, intent.weightKg)
            is MyPageIntent.WeightErrorShown -> setState { copy(weightError = null) }

            // TODO(mypage): 서브 화면 구현 후 라우팅 연결.
            is MyPageIntent.NotificationSettingClicked -> Unit
            is MyPageIntent.GroupSettingClicked -> Unit
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

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        try {
            val profileDeferred = async { myPageRepository.getProfile() }
            val weightDeferred = async { weightSummaryOrNull() }
            val p = profileDeferred.await()
            val w = weightDeferred.await()
            setState {
                copy(
                    nickname = p.nickname,
                    profileImageUrl = p.profileImageUrl,
                    daysTogether = p.daysTogether,
                    weight = w,
                    isLoading = false,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 프로필 로드 실패해도 화면은 유지(빈 값). 로딩만 해제.
            setState { copy(isLoading = false) }
        }
    }
}
