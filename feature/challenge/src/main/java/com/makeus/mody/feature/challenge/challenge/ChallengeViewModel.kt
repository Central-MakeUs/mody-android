package com.makeus.mody.feature.challenge.challenge

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeIntent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val groupRepository: GroupRepository,
    private val sessionRepository: SessionRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<ChallengeState, ChallengeIntent>(ChallengeState()) {

    /** 현재 보고 있는 그룹. 피드와 동일 규칙(마지막 선택 그룹 > 첫 그룹)으로 결정. */
    private var currentGroupId: Long? = null

    override suspend fun processIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.ScreenEntered -> load()
            is ChallengeIntent.SubTabSelected -> setState { copy(selectedSubTab = intent.tab) }
            is ChallengeIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))
            is ChallengeIntent.NudgeClicked -> nudge(intent.memberId)
            is ChallengeIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    /** 그룹 결정 후 요약/버디 목록 병렬 조회. 부분 실패는 이전 값 유지(전체 에러로 막지 않음). */
    private fun load() = viewModelScope.launch {
        // 첫 로드만 스켈레톤, 탭 복귀 재조회는 조용히 갱신(깜빡임 방지).
        setState { copy(isLoading = summary == null) }
        val groupId = resolveGroupId()
        if (groupId == null) {
            setState { copy(isLoading = false) }
            return@launch
        }
        // async 를 launch 자식으로 두고 던지면 부모로 전파(크래시) → runCatching 흡수 + supervisorScope.
        val (summary, buddies) = supervisorScope {
            val summaryDeferred = async {
                runCatching { challengeRepository.getSummary(groupId) }.getOrNull()
            }
            val buddiesDeferred = async {
                runCatching { challengeRepository.getNudgeTargets(groupId) }.getOrNull()
            }
            summaryDeferred.await() to buddiesDeferred.await()
        }
        setState {
            copy(
                isLoading = false,
                summary = summary ?: this.summary,
                buddies = buddies ?: this.buddies,
            )
        }
    }

    /** 마지막 선택 그룹(세션) > 첫 그룹. 피드의 그룹 결정 규칙과 동일. */
    private suspend fun resolveGroupId(): Long? {
        currentGroupId?.let { return it }
        val groups = runCatching { groupRepository.getMyGroups() }.getOrNull() ?: return null
        val lastGroupId = runCatching { sessionRepository.getLastGroupId() }.getOrNull()
            ?.takeIf { id -> groups.any { it.groupId == id } }
        return (lastGroupId ?: groups.firstOrNull()?.groupId).also { currentGroupId = it }
    }

    private fun nudge(memberId: Long) = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        if (memberId in currentState.nudgingMemberIds) return@launch
        setState { copy(nudgingMemberIds = nudgingMemberIds + memberId) }
        try {
            challengeRepository.nudge(groupId, memberId)
            setState { copy(nudgingMemberIds = nudgingMemberIds - memberId) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(
                    nudgingMemberIds = nudgingMemberIds - memberId,
                    error = (e as? HttpResponseException)?.msg
                        ?: "콕 찌르기에 실패했어요. 다시 시도해주세요.",
                )
            }
        }
    }
}
