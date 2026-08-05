package com.makeus.mody.feature.challenge.challenge

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeIntent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    private val healthRepository: HealthRepository,
    private val onboardingRepository: OnboardingRepository,
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
            is ChallengeIntent.StepRefreshClicked -> refreshStep()
            is ChallengeIntent.HealthPermissionRequestLaunched ->
                setState { copy(healthPermissionRequest = null) }
            is ChallengeIntent.HealthPermissionResult -> onHealthPermissionResult(intent.granted)
            // TODO(challenge): 걸음 수 챌린지 변경(walk/choose)·주간 챌린지 상세 화면 연결 (후속 PR).
            is ChallengeIntent.ChangeStepChallengeClicked -> Unit
            is ChallengeIntent.WeeklyChallengeClicked -> Unit
            is ChallengeIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    /** 그룹 결정 후 요약/버디 목록 병렬 조회. 부분 실패는 이전 값 유지(전체 에러로 막지 않음). */
    private fun load() = viewModelScope.launch {
        // 첫 로드만 스켈레톤, 탭 복귀 재조회는 조용히 갱신(깜빡임 방지).
        setState { copy(isLoading = summary == null) }
        val previousGroupId = currentGroupId
        val groupId = resolveGroupId()
        if (groupId == null) {
            setState { copy(isLoading = false) }
            return@launch
        }
        currentGroupId = groupId
        // 피드에서 그룹을 바꾸고 넘어온 경우. 아래 병합이 `?: this.summary` 로 이전 값을
        // 유지하므로, 여기서 비우지 않으면 새 그룹 조회가 실패한 항목에 옛 그룹 기록이 남는다.
        if (previousGroupId != null && previousGroupId != groupId) {
            setState { ChallengeState(selectedSubTab = selectedSubTab, isLoading = true) }
        }
        // async 를 launch 자식으로 두고 던지면 부모로 전파(크래시) → runCatching 흡수 + supervisorScope.
        data class Loaded(
            val summary: ChallengeSummary?,
            val buddies: List<NudgeTarget>?,
            val step: StepChallengeStatus?,
            val rankings: List<StepRanking>?,
            val weekly: List<WeeklyChallenge>?,
        )
        val loaded = supervisorScope {
            val summaryDeferred = async {
                runCatching { challengeRepository.getSummary(groupId) }.getOrNull()
            }
            val buddiesDeferred = async {
                runCatching { challengeRepository.getNudgeTargets(groupId) }.getOrNull()
            }
            val stepDeferred = async {
                runCatching { challengeRepository.getStepChallenge(groupId) }.getOrNull()
            }
            val rankingsDeferred = async {
                runCatching { challengeRepository.getStepRankings(groupId) }.getOrNull()
            }
            val weeklyDeferred = async {
                runCatching { challengeRepository.getWeeklyChallenges(groupId) }.getOrNull()
            }
            Loaded(
                summary = summaryDeferred.await(),
                buddies = buddiesDeferred.await(),
                step = stepDeferred.await(),
                rankings = rankingsDeferred.await(),
                weekly = weeklyDeferred.await(),
            )
        }
        setState {
            copy(
                isLoading = false,
                summary = loaded.summary ?: this.summary,
                buddies = loaded.buddies ?: this.buddies,
                stepChallenge = loaded.step ?: this.stepChallenge,
                stepRankings = loaded.rankings ?: this.stepRankings,
                weeklyChallenges = loaded.weekly ?: this.weeklyChallenges,
            )
        }
        // 탭 진입(= 앱 실행 후 첫 진입 포함)마다 오늘 걸음 수를 서버에 반영.
        syncSteps(groupId, askPermission = false)
    }

    /** 걸음 수 새로고침 — 건강 데이터 재동기화 후 현황 + 순위 재조회. */
    private fun refreshStep() = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        // 수동 새로고침은 사용자가 의도한 행동이므로 권한이 없으면 다시 물어본다.
        syncSteps(groupId, askPermission = true)
        val step = runCatching { challengeRepository.getStepChallenge(groupId) }.getOrNull()
        val rankings = runCatching { challengeRepository.getStepRankings(groupId) }.getOrNull()
        setState {
            copy(
                stepChallenge = step ?: stepChallenge,
                stepRankings = rankings ?: stepRankings,
            )
        }
    }

    /**
     * 오늘 걸음 수를 읽어 서버에 반영한다.
     *
     * 권한이 없을 때: [askPermission] 이 true 면 무조건, false 면 아직 물어본 적 없을 때만
     * 시스템 권한 요청을 띄운다(탭 재진입마다 팝업이 뜨는 것 방지).
     */
    private suspend fun syncSteps(groupId: Long, askPermission: Boolean) {
        if (healthRepository.availability() != HealthAvailability.AVAILABLE) return
        if (runCatching { healthRepository.hasStepPermission() }.getOrDefault(false)) {
            uploadTodaySteps(groupId)
            return
        }
        val alreadyAsked = runCatching { sessionRepository.getHealthPermissionAsked() }
            .getOrDefault(false)
        if (!askPermission && alreadyAsked) return
        runCatching { sessionRepository.saveHealthPermissionAsked() }
        setState { copy(healthPermissionRequest = healthRepository.stepPermissions) }
    }

    /** 권한 요청 결과 — 서버에 연동 여부를 남기고, 허용됐으면 바로 동기화. */
    private fun onHealthPermissionResult(granted: Boolean) = viewModelScope.launch {
        setState { copy(healthPermissionRequest = null) }
        // 연동 여부 기록 실패는 사용자 흐름을 막을 이유가 없어 조용히 넘긴다.
        runCatching { onboardingRepository.reportHealthConnection(granted) }
        if (!granted) return@launch
        val groupId = currentGroupId ?: return@launch
        uploadTodaySteps(groupId)
        val step = runCatching { challengeRepository.getStepChallenge(groupId) }.getOrNull()
        val rankings = runCatching { challengeRepository.getStepRankings(groupId) }.getOrNull()
        setState {
            copy(
                stepChallenge = step ?: stepChallenge,
                stepRankings = rankings ?: stepRankings,
            )
        }
    }

    /**
     * 건강 데이터의 오늘 걸음 수를 서버에 upsert 하고, 응답의 그룹 누적값을 즉시 반영한다.
     * 진행 중인 걸음 수 챌린지가 없으면 서버가 실패를 주므로 조용히 넘긴다.
     */
    private suspend fun uploadTodaySteps(groupId: Long) {
        setState { copy(isSyncingSteps = true) }
        val steps = runCatching { healthRepository.readTodayStepCount() }.getOrNull()
        if (steps != null) {
            runCatching {
                challengeRepository.upsertStepRecord(
                    groupId = groupId,
                    recordedOn = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    stepCount = steps,
                )
            }.onSuccess { result ->
                setState {
                    copy(
                        stepChallenge = stepChallenge?.copy(
                            currentStepCount = result.currentStepCount,
                            targetStepCount = result.targetStepCount,
                        ),
                    )
                }
            }
        }
        setState { copy(isSyncingSteps = false) }
    }

    /**
     * 마지막 선택 그룹(세션) > 첫 그룹. 피드의 그룹 결정 규칙과 동일.
     *
     * 캐시해두고 재사용하지 않는다. 피드에서 그룹을 바꾸면 세션의 마지막 그룹만 갱신되고
     * (FeedViewModel.selectGroup) 이 ViewModel 은 탭 전환에도 살아있으므로, 캐시를 쓰면
     * 앱을 재시작할 때까지 이전 그룹 데이터를 계속 보여주게 된다.
     * 조회 실패 시에만 직전 그룹을 유지한다.
     */
    private suspend fun resolveGroupId(): Long? {
        val groups = runCatching { groupRepository.getMyGroups() }.getOrNull() ?: return currentGroupId
        val lastGroupId = runCatching { sessionRepository.getLastGroupId() }.getOrNull()
            ?.takeIf { id -> groups.any { it.groupId == id } }
        return lastGroupId ?: groups.firstOrNull()?.groupId
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
