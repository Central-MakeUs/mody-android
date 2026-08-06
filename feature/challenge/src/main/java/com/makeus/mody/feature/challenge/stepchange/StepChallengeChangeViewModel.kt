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
            is StepChallengeChangeIntent.OptionClicked -> setState {
                copy(pendingOption = options.firstOrNull { it.challengeId == intent.challengeId })
            }
            is StepChallengeChangeIntent.ChangeCancelled -> setState { copy(pendingOption = null) }
            is StepChallengeChangeIntent.ChangeConfirmed -> change()
            is StepChallengeChangeIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        val options = runCatching { challengeRepository.getStepChallengeOptions(groupId) }
            .getOrNull()
        setState { copy(isLoading = false, options = options ?: this.options) }
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
