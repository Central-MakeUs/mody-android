package com.makeus.mody.feature.challenge.challenge

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.core.domain.model.NudgeButtonStatus
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.notification.UnreadNotificationStore
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.domain.usecase.SyncTodayStepsUseCase
import com.makeus.mody.core.navigation.ChallengeGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.core.navigation.PendingStreakTabHolder
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeIntent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeState
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeSubTab
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
    private val healthRepository: HealthRepository,
    private val onboardingRepository: OnboardingRepository,
    private val navigationHelper: NavigationHelper,
    private val pendingStreakTabHolder: PendingStreakTabHolder,
    private val unreadNotificationStore: UnreadNotificationStore,
    private val syncTodaySteps: SyncTodayStepsUseCase,
) : BaseViewModel<ChallengeState, ChallengeIntent>(ChallengeState()) {

    /** 현재 보고 있는 그룹. 피드와 동일 규칙(마지막 선택 그룹 > 첫 그룹)으로 결정. */
    private var currentGroupId: Long? = null

    init {
        // 상단바 알림 뱃지 — 값은 앱 전역 단일 소스(다른 탭·푸시 수신과 표시가 어긋나지 않게).
        viewModelScope.launch {
            unreadNotificationStore.hasUnread.collect { hasUnread ->
                setState { copy(hasUnreadNotification = hasUnread) }
            }
        }
    }

    override suspend fun processIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.ScreenEntered -> {
                // 피드 "콕 찌르기 하러 가기" 로 넘어온 경우. ViewModel 은 탭 전환에도 살아남아
                // 직전에 보던 서브탭이 남으므로, 요청이 있으면 연속 기록으로 되돌린다.
                if (pendingStreakTabHolder.consume()) {
                    setState { copy(selectedSubTab = ChallengeSubTab.STREAK) }
                }
                load()
                // 알림함을 보고 돌아온 경우가 있어 진입마다 뱃지도 재조회.
                viewModelScope.launch { unreadNotificationStore.refresh() }
            }
            is ChallengeIntent.SubTabSelected -> setState { copy(selectedSubTab = intent.tab) }
            is ChallengeIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))
            is ChallengeIntent.NudgeClicked -> nudge(intent.memberId)
            is ChallengeIntent.StepRefreshClicked -> refreshStep()
            is ChallengeIntent.HealthPermissionRequestLaunched ->
                setState { copy(healthPermissionRequest = null) }
            is ChallengeIntent.HealthPermissionResult -> onHealthPermissionResult(intent.granted)
            is ChallengeIntent.HealthSettingsClicked -> setState {
                copy(
                    showHealthPermissionGuide = false,
                    healthSettingsRequest = healthRepository.availability(),
                )
            }
            is ChallengeIntent.HealthSettingsLaunched ->
                setState { copy(healthSettingsRequest = null) }
            is ChallengeIntent.HealthPermissionGuideDismissed ->
                setState { copy(showHealthPermissionGuide = false) }
            is ChallengeIntent.ChangeStepChallengeClicked -> currentGroupId?.let { groupId ->
                navigationHelper.navigate(
                    NavigationEvent.To(ChallengeGraph.StepChallengeChangeRoute(groupId)),
                )
            }
            is ChallengeIntent.WeeklyChallengeClicked ->
                navigateToWeeklyDetail(intent.groupChallengeId)
            is ChallengeIntent.ToastShown -> setState { copy(toastMessage = null) }
            is ChallengeIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    /**
     * 주간 챌린지 상세로 이동.
     *
     * 제목·마감 요일을 인자로 넘긴다. 상세 API 는 목록이 주지 않는 `challengeId` 를 요구해
     * 호출할 수 없어, 목록이 이미 들고 있는 값을 그대로 전달한다.
     */
    private fun navigateToWeeklyDetail(groupChallengeId: Long) {
        val groupId = currentGroupId ?: return
        val challenge = currentState.weeklyChallenges
            .firstOrNull { it.groupChallengeId == groupChallengeId } ?: return
        navigationHelper.navigate(
            NavigationEvent.To(
                ChallengeGraph.WeeklyChallengeDetailRoute(
                    groupId = groupId,
                    groupChallengeId = groupChallengeId,
                    title = challenge.title,
                    deadlineDayOfWeek = challenge.deadlineDayOfWeek,
                ),
            ),
        )
    }

    /** 그룹 결정 후 요약/버디 목록 병렬 조회. 부분 실패는 이전 값 유지(전체 에러로 막지 않음). */
    private fun load() = viewModelScope.launch {
        // 첫 로드만 스켈레톤, 탭 복귀 재조회는 조용히 갱신(깜빡임 방지).
        setState { copy(isLoading = summary == null) }
        val previousGroupId = currentGroupId
        // 조회 실패 시엔 null — 직전 그룹을 그대로 쓴다(인원 수도 이전 값 유지).
        val group = resolveGroup()
        val groupId = group?.groupId ?: currentGroupId
        if (groupId == null) {
            setState { copy(isLoading = false) }
            return@launch
        }
        currentGroupId = groupId
        // 피드에서 그룹을 바꾸고 넘어온 경우. 아래 병합이 `?: this.summary` 로 이전 값을
        // 유지하므로, 여기서 비우지 않으면 새 그룹 조회가 실패한 항목에 옛 그룹 기록이 남는다.
        if (previousGroupId != null && previousGroupId != groupId) {
            // 알림 뱃지는 그룹과 무관하므로 넘긴다. 초기값(false)으로 되돌리면 store 의 StateFlow 가
            // 같은 값을 다시 내보내지 않아 뱃지가 켜질 때까지 사라진 채로 남는다.
            setState {
                ChallengeState(
                    selectedSubTab = selectedSubTab,
                    hasUnreadNotification = hasUnreadNotification,
                    isLoading = true,
                )
            }
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
                loadOrNull("summary") { challengeRepository.getSummary(groupId) }
            }
            val buddiesDeferred = async {
                loadOrNull("nudgeTargets") { challengeRepository.getNudgeTargets(groupId) }
            }
            val stepDeferred = async {
                loadOrNull("stepChallenge") { challengeRepository.getStepChallenge(groupId) }
            }
            val rankingsDeferred = async {
                loadOrNull("stepRankings") { challengeRepository.getStepRankings(groupId) }
            }
            val weeklyDeferred = async {
                loadOrNull("weeklyChallenges") { challengeRepository.getWeeklyChallenges(groupId) }
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
                buddiesLoaded = buddiesLoaded || loaded.buddies != null,
                stepChallenge = loaded.step ?: this.stepChallenge,
                stepRankings = loaded.rankings ?: this.stepRankings,
                weeklyChallenges = loaded.weekly ?: this.weeklyChallenges,
                weeklyLoaded = loaded.weekly != null,
                groupMemberCount = group?.memberCount ?: this.groupMemberCount,
            )
        }
        // 탭 진입(= 앱 실행 후 첫 진입 포함)마다 오늘 걸음 수를 서버에 반영.
        // 방금 받은 현황을 넘겨 use case 가 step/current 를 다시 부르지 않게 한다.
        syncSteps(groupId, askPermission = false, challenge = loaded.step)
    }

    /** 걸음 수 새로고침 — 건강 데이터 재동기화 후 현황 + 순위 재조회. */
    private fun refreshStep() = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        val step = loadOrNull("stepChallenge") { challengeRepository.getStepChallenge(groupId) }
        val rankings = loadOrNull("stepRankings") { challengeRepository.getStepRankings(groupId) }
        setState {
            copy(
                stepChallenge = step ?: stepChallenge,
                stepRankings = rankings ?: stepRankings,
            )
        }
        // 동기화가 마지막 — 순서를 바꾸면 조회 결과가 방금 반영한 실제 걸음 수를 덮어쓴다.
        // 수동 새로고침은 사용자가 의도한 행동이므로 권한이 없으면 다시 물어본다.
        syncSteps(groupId, askPermission = true, challenge = step)
    }

    /**
     * 오늘 걸음 수를 읽어 서버에 반영한다.
     *
     * 권한이 없을 때: [askPermission] 이 true 면 무조건, false 면 아직 물어본 적 없을 때만
     * 시스템 권한 요청을 띄운다(탭 재진입마다 팝업이 뜨는 것 방지).
     *
     * 단, 나 혼자인 그룹에선 권한을 묻지 않는다 — 아래 [ChallengeState.isSoloGroup] 주석 참고.
     */
    private suspend fun syncSteps(
        groupId: Long,
        askPermission: Boolean,
        challenge: StepChallengeStatus? = null,
    ) {
        if (healthRepository.availability() != HealthAvailability.AVAILABLE) return
        if (runCatching { healthRepository.hasStepPermission() }.getOrDefault(false)) {
            uploadTodaySteps(groupId, challenge)
            return
        }
        // 나 혼자인 그룹이면 화면이 통째로 SoloGroupEmpty 라 걸음 수 UI 가 하나도 없다.
        // 그 상태에서 권한만 띄우면 "보여주는 기능 없이 건강 데이터만 가져가는 앱"이 된다
        // — 실제로 이 사유로 스토어 심사에서 반려됐다(헬스 커넥트 '최소 범위' 위반).
        // 이미 허용된 경우(위 분기)는 그대로 올린다 — 버디가 들어오는 순간 기록이 이어져야 한다.
        if (currentState.isSoloGroup) return
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
        if (!granted) {
            // 거부를 조용히 넘기면 걸음 수가 0 인 채로 화면이 그대로다. 두 번 거부한 뒤로는
            // 시스템이 다이얼로그 자체를 안 띄우므로, 사용자는 새로고침이 고장 났다고 본다.
            setState { copy(showHealthPermissionGuide = true) }
            return@launch
        }
        val groupId = currentGroupId ?: return@launch
        val step = loadOrNull("stepChallenge") { challengeRepository.getStepChallenge(groupId) }
        val rankings = loadOrNull("stepRankings") { challengeRepository.getStepRankings(groupId) }
        setState {
            copy(
                stepChallenge = step ?: stepChallenge,
                stepRankings = rankings ?: stepRankings,
            )
        }
        // 조회 뒤에 올려야 방금 읽은 걸음 수가 조회 결과에 덮이지 않는다.
        uploadTodaySteps(groupId, step)
    }

    /**
     * 걸음 수를 서버에 반영하고, 서버가 재계산한 누적값을 게이지에 즉시 옮긴다.
     *
     * 실제 동기화(카운트 시작 시각 반영·날짜별 백필)는 [syncTodaySteps] 가 한다.
     *
     * 올린 날이 하나도 없으면 게이지를 건드리지 않는다. 누적값은 upsert 응답에만 실려
     * 오므로 그때 `currentStepCount` 가 null 인데, 이걸 0 으로 대신 채우면 방금 조회한
     * 서버 값이 0 으로 덮인다. 업로드가 안 일어나는 건 예외 상황이 아니라 일상이다 —
     * 목표를 이미 달성해 서버가 upsert 를 거부하거나, 오늘·어제 걸음이 0 이면 늘 이
     * 경로다.
     */
    private suspend fun uploadTodaySteps(groupId: Long, challenge: StepChallengeStatus? = null) {
        setState { copy(isSyncingSteps = true) }
        // runCatching 은 취소까지 삼켜 ViewModel 정리 시 코루틴이 정상 종료로 보인다.
        val result = try {
            syncTodaySteps(groupId, challenge)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        val serverTotal = result?.currentStepCount
        val serverTarget = result?.targetStepCount
        if (serverTotal != null) {
            setState {
                copy(
                    stepChallenge = stepChallenge?.let { current ->
                        current.copy(
                            currentStepCount = serverTotal,
                            targetStepCount = serverTarget ?: current.targetStepCount,
                        )
                    },
                )
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
     *
     * id 만이 아니라 그룹 자체를 돌려준다 — 인원 수(혼자인 그룹 판정)가 여기에만 실려 온다.
     * 조회 실패(null) 시엔 호출부가 직전 그룹을 유지한다.
     *
     * 취소는 실패로 뭉뚱그리지 않고 그대로 올린다. runCatching 이 CancellationException 까지
     * 삼키면 화면을 떠난 뒤에도 호출부가 계속 진행해 setState 를 때린다.
     */
    private suspend fun resolveGroup(): Group? {
        val groups = orNullUnlessCancelled { groupRepository.getMyGroups() } ?: return null
        val lastGroupId = orNullUnlessCancelled { sessionRepository.getLastGroupId() }
        return groups.firstOrNull { it.groupId == lastGroupId } ?: groups.firstOrNull()
    }

    /** 실패는 null, 취소는 전파. */
    private suspend fun <T> orNullUnlessCancelled(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    /**
     * 콕 찌르기. 결과 상태는 서버가 응답으로 주므로 그 버디의 상태만 갈아끼운다
     * (목록 재조회 없음).
     *
     * 요청 시점의 groupId 를 붙들고, 응답이 온 뒤 현재 그룹과 같은지 확인한다 — 피드에서
     * 그룹을 바꾸면 [load] 가 currentGroupId 를 갈아끼우는데, 그 뒤 늦게 도착한 응답을 그대로
     * 반영하면 memberId 가 겹치는 다른 그룹의 버디 상태를 바꾸고 엉뚱한 토스트가 뜬다.
     * (`FeedViewModel` 이 날짜에 대해 하는 것과 같은 방어.)
     *
     * 버리고 나가도 nudgingMemberIds 는 남지 않는다 — 그룹이 바뀌면 [load] 가 상태를
     * 새로 만들면서 함께 비워진다.
     */
    private fun nudge(memberId: Long) = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        if (memberId in currentState.nudgingMemberIds) return@launch
        val buddy = currentState.buddies.firstOrNull { it.memberId == memberId } ?: return@launch
        if (buddy.nudgeStatus != NudgeButtonStatus.AVAILABLE) return@launch
        setState { copy(nudgingMemberIds = nudgingMemberIds + memberId) }
        try {
            val status = challengeRepository.nudge(groupId, memberId)
            if (groupId != currentGroupId) return@launch
            setState {
                copy(
                    nudgingMemberIds = nudgingMemberIds - memberId,
                    buddies = buddies.map {
                        if (it.memberId == memberId) it.copy(nudgeStatus = status) else it
                    },
                    toastMessage = "${buddy.nickname}님에게 알림을 보냈어요",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (groupId != currentGroupId) return@launch
            setState {
                copy(
                    nudgingMemberIds = nudgingMemberIds - memberId,
                    error = (e as? HttpResponseException)?.msg
                        ?: "콕 찌르기에 실패했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    /**
     * 조회 실패를 null 로 바꿔 다른 항목 로딩을 막지 않되, **로그는 남긴다.**
     *
     * 실패를 조용히 삼키면 화면상 "빈 응답"과 구분되지 않아, 서버에 데이터가 없는 건지
     * 호출이 깨진 건지 알 수 없다. 취소는 실패가 아니므로 그대로 전파한다.
     */
    private inline fun <T> loadOrNull(name: String, block: () -> T): T? =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "$name 조회 실패 — 화면은 빈 상태로 남는다", e)
            null
        }

    private companion object {
        const val TAG = "ChallengeViewModel"
    }
}
