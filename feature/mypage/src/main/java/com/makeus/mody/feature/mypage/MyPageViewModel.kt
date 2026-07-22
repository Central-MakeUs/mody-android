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

            // TODO(mypage): 서브 화면 구현 후 라우팅 연결.
            is MyPageIntent.NotificationSettingClicked -> Unit
            is MyPageIntent.GroupSettingClicked -> Unit
            is MyPageIntent.HealthDataSettingClicked -> Unit
        }
    }

    private fun recordWeight(recordedOn: String, weightKg: Double) = viewModelScope.launch {
        if (currentState.isRecordingWeight) return@launch
        setState { copy(isRecordingWeight = true) }
        try {
            myPageRepository.recordWeight(recordedOn, weightKg)
            // 저장 후 요약 갱신(현재/이전 반영). 시트 닫기.
            val w = runCatching { myPageRepository.getWeightSummary() }.getOrNull()
            setState { copy(weight = w ?: weight, isRecordingWeight = false, showWeightSheet = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 실패해도 시트는 닫고(중복 저장 방지) 로딩만 해제. 카드 값은 유지.
            setState { copy(isRecordingWeight = false, showWeightSheet = false) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        try {
            val profileDeferred = async { myPageRepository.getProfile() }
            val weightDeferred = async { runCatching { myPageRepository.getWeightSummary() }.getOrNull() }
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
