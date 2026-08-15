package com.makeus.mody.feature.challenge.stepchange

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.navigation.ChallengeGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.challenge.stepchange.contract.StepChallengeChangeIntent
import com.makeus.mody.feature.challenge.stepchange.contract.StepChallengeChangeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class StepChallengeChangeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val navigationHelper: NavigationHelper,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<StepChallengeChangeState, StepChallengeChangeIntent>(
    StepChallengeChangeState(),
) {

    private val groupId =
        savedStateHandle.toRoute<ChallengeGraph.StepChallengeChangeRoute>().groupId

    override suspend fun processIntent(intent: StepChallengeChangeIntent) {
        when (intent) {
            is StepChallengeChangeIntent.ScreenEntered -> load()
            is StepChallengeChangeIntent.BackClicked ->
                navigationHelper.navigate(NavigationEvent.Up)
            // 고를 수 없는 챌린지는 확인 다이얼로그까지 가지 않는다. 화면이 탭을 막고
            // 있지만 규칙 자체는 여기 둔다 — 화면만 믿으면 목록이 다른 경로로 열릴 때
            // (딥링크·재사용) 그대로 새어 나간다.
            //   selected  이미 진행 중 — 바꿀 게 없다
            //   completed 이미 달성함 — 다시 고르면 기록이 초기화되는데 얻는 게 없다
            is StepChallengeChangeIntent.OptionClicked -> setState {
                val target = options.firstOrNull { it.challengeId == intent.challengeId }
                if (target == null || target.selected || target.completed) this
                else copy(pendingOption = target)
            }
            is StepChallengeChangeIntent.ChangeCancelled -> setState { copy(pendingOption = null) }
            is StepChallengeChangeIntent.ChangeConfirmed -> change()
            is StepChallengeChangeIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        try {
            val loaded = challengeRepository.getStepChallengeOptions(groupId)
            setState { copy(isLoading = false, options = loaded) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 조회 실패는 알린다 — 여기서 조용히 넘기면 목록이 비어 "선택지가 없는 화면"으로 보인다.
            setState {
                copy(
                    isLoading = false,
                    error = (e as? HttpResponseException)?.msg
                        ?: "챌린지 목록을 불러오지 못했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    /** 교체 성공하면 챌린지 탭으로 돌아간다. 탭이 재진입 시 재조회하므로 결과는 자동 반영. */
    private fun change() = viewModelScope.launch {
        val option = currentState.pendingOption ?: return@launch
        if (currentState.isChanging) return@launch
        setState { copy(isChanging = true) }
        try {
            challengeRepository.changeStepChallenge(groupId, option.challengeId)
            setState { copy(isChanging = false, pendingOption = null) }
            navigationHelper.navigate(NavigationEvent.Up)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(
                    isChanging = false,
                    pendingOption = null,
                    error = (e as? HttpResponseException)?.msg
                        ?: "챌린지 변경에 실패했어요. 다시 시도해주세요.",
                )
            }
        }
    }
}
