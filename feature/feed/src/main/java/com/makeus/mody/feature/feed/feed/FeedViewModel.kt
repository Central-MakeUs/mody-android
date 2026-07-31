package com.makeus.mody.feature.feed.feed

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.navigation.FeedGraph
import com.makeus.mody.core.navigation.GroupEntrySource
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.core.navigation.RecordGraph
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState
import com.makeus.mody.feature.feed.feed.contract.GroupUi
import com.makeus.mody.feature.feed.feed.contract.WeekDayUi
import com.makeus.mody.feature.feed.feed.contract.toFeedCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val feedRepository: FeedRepository,
    private val navigationHelper: NavigationHelper,
    private val pendingGroupSelectionHolder: PendingGroupSelectionHolder,
    private val sessionRepository: SessionRepository,
    private val myPageRepository: MyPageRepository,
    remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<FeedState, FeedIntent>(FeedState()) {

    init {
        // 챌린지 기능 플래그 반영 — Phase 1 에선 콕 찌르기 등 챌린지 접점 숨김.
        viewModelScope.launch {
            remoteConfigRepository.phaseTwoFeaturesEnabled.collect { enabled ->
                setState { copy(phaseTwoFeaturesEnabled = enabled) }
            }
        }
    }

    /** 현재 선택된 날짜 (피드 조회 기준). */
    private var selectedDate: LocalDate = LocalDate.now()

    /** 주간 스트립에 표시 중인 주의 일요일. */
    private var weekStart: LocalDate = sundayOf(selectedDate)

    /** 현재 보고 있는 그룹. 선택 시마다 세션에 저장해 재진입 때 복원. */
    private var currentGroupId: Long? = null

    /** 내가 속한 전체 그룹 (선택 시트용). */
    private var myGroups: List<Group> = emptyList()

    /** 날짜별 기록 유무 캐시 (주 이동 시 누적). */
    private var recordDates: Map<LocalDate, Boolean> = emptyMap()

    /** 현재 날짜 피드의 다음 커서 (무한 스크롤). null 이면 더 없음. */
    private var feedsCursor: Long? = null

    /** 날짜 연속 선택(주간 스와이프 등) 시 API 난사 방지용 디바운스 job. */
    private var selectDayJob: Job? = null

    init {
        setState {
            copy(
                weekLabel = formatWeekLabel(weekStart),
                weekDays = buildWeekDays(),
                canGoNextWeek = canGoNextWeek(),
            )
        }
        loadMyGroup()
        loadMyMemberId()
        observePendingGroupSelection()
    }

    /** 내 memberId 조회 — 내 게시물엔 신고 메뉴를 숨기기 위한 판별값. 실패 시 null 유지(신고 메뉴 전체 미노출). */
    private fun loadMyMemberId() = viewModelScope.launch {
        runCatching { myPageRepository.getProfile() }
            .onSuccess { setState { copy(myMemberId = it.memberId) } }
    }

    /**
     * 그룹홈 알림 딥링크로 대기 중인 groupId 가 생기면 해당 그룹으로 전환.
     * 앱 실행 중 알림 탭(warm) 대응. 콜드 스타트는 loadMyGroup 에서 첫 선택 시 반영한다.
     * (myGroups 로딩 전엔 flow 값이 있어도 skip → loadMyGroup 이 처리.)
     */
    private fun observePendingGroupSelection() = viewModelScope.launch {
        pendingGroupSelectionHolder.pendingGroupId.collect { groupId ->
            if (groupId == null) return@collect
            if (myGroups.any { it.groupId == groupId }) {
                pendingGroupSelectionHolder.consume()
                selectGroup(groupId)
            }
        }
    }

    override suspend fun processIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.ScreenResumed -> refresh()
            is FeedIntent.PrevWeekClicked -> moveWeek(-1)
            is FeedIntent.NextWeekClicked -> moveWeek(1)
            is FeedIntent.DaySelected -> selectDay(intent.date)

            is FeedIntent.FabClicked -> setState { copy(isFabExpanded = !isFabExpanded) }
            is FeedIntent.FabDismissed -> setState { copy(isFabExpanded = false) }

            is FeedIntent.GroupSelectorClicked -> setState { copy(isGroupSheetVisible = true) }
            is FeedIntent.GroupSelected -> selectGroup(intent.groupId)
            is FeedIntent.GroupSheetDismissed -> setState { copy(isGroupSheetVisible = false) }
            is FeedIntent.AddGroupClicked ->
                setState { copy(isGroupSheetVisible = false, isAddGroupDialogVisible = true) }
            is FeedIntent.JoinGroupClicked -> {
                setState { copy(isAddGroupDialogVisible = false) }
                navigationHelper.navigate(
                    NavigationEvent.To(GroupGraph.GroupEntryRoute(source = GroupEntrySource.Feed)),
                )
            }
            is FeedIntent.CreateGroupClicked -> {
                setState { copy(isAddGroupDialogVisible = false) }
                navigationHelper.navigate(NavigationEvent.To(GroupGraph.CreateGroupRoute))
            }
            is FeedIntent.AddGroupDialogDismissed -> setState { copy(isAddGroupDialogVisible = false) }
            is FeedIntent.LoadMoreFeeds -> loadMoreFeeds()

            is FeedIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))
            // TODO(feed): 콕찌르기 연결
            is FeedIntent.PokeClicked -> Unit
            is FeedIntent.FeedCardClicked -> {
                val groupId = currentGroupId ?: return
                navigationHelper.navigate(
                    NavigationEvent.To(
                        FeedGraph.RecordDetailRoute(groupId, intent.id, selectedDate.toString()),
                    ),
                )
            }
            is FeedIntent.ReportClicked -> setState { copy(reportTargetRecordId = intent.recordId) }
            is FeedIntent.ReportDialogDismissed -> setState { copy(reportTargetRecordId = null) }
            is FeedIntent.ReportConfirmed -> reportRecord()
            is FeedIntent.ReportCompleteConfirmed -> setState { copy(showReportComplete = false) }
            is FeedIntent.ReportErrorShown -> setState { copy(reportError = null) }
            is FeedIntent.WriteExerciseClicked -> {
                setState { copy(isFabExpanded = false) }
                navigationHelper.navigate(NavigationEvent.To(RecordGraph.HealthRoute))
            }
            is FeedIntent.WriteMealClicked -> {
                setState { copy(isFabExpanded = false) }
                navigationHelper.navigate(NavigationEvent.To(RecordGraph.FoodRoute))
            }
        }
    }

    /**
     * 내 그룹 조회 → 현재 그룹 결정.
     * 우선순위: 그룹홈 알림 딥링크 대기 groupId > 마지막 선택 그룹(세션 저장) > 첫 그룹.
     * (탈퇴/이동으로 더는 내 그룹이 아닌 저장값은 무시.)
     */
    private fun loadMyGroup() = viewModelScope.launch {
        runCatching { groupRepository.getMyGroups() }
            .onSuccess { groups ->
                // 속한 그룹이 없으면(다른 기기에서 전부 나감 등) 그룹 참여/생성으로 강제 이동.
                // 백스택 제거 → 피드 복귀 불가. 세션 플래그도 내려 재접속 시 GROUP 시작.
                if (groups.isEmpty()) {
                    runCatching {
                        sessionRepository.saveStatus(
                            sessionRepository.getStatus().copy(
                                groupOnboardingCompleted = false,
                                mainAccessible = false,
                            ),
                        )
                    }
                    navigationHelper.navigate(NavigationEvent.To(
                        GroupGraph.GroupEntryRoute(source = GroupEntrySource.NoGroup),
                        popUpTo = true,
                    ))
                    return@onSuccess
                }
                myGroups = groups
                val pendingGroupId = pendingGroupSelectionHolder.pendingGroupId.value
                    ?.takeIf { id -> groups.any { it.groupId == id } }
                val lastGroupId = runCatching { sessionRepository.getLastGroupId() }.getOrNull()
                    ?.takeIf { id -> groups.any { it.groupId == id } }
                val group = (pendingGroupId ?: lastGroupId)
                    ?.let { id -> groups.first { it.groupId == id } }
                    ?: groups.firstOrNull()
                    ?: return@onSuccess
                if (pendingGroupId != null) pendingGroupSelectionHolder.consume()
                currentGroupId = group.groupId
                saveLastGroup(group.groupId)
                setState { copy(groupName = group.name, groups = buildGroupUis()) }
                loadCalendar()
                loadFeeds(selectedDate)
            }
        // 실패 시 그룹명/기록 점 미표시. TODO(feed): 에러 노출 정책 정해지면 처리.
    }

    /**
     * 화면 복귀 시 재조회. 기록 작성 후 돌아오면 새 기록/점이 반영되도록
     * 현재 그룹의 캘린더 + 선택 날짜 피드를 다시 불러온다.
     * 첫 진입(그룹 미로딩)에는 init 의 loadMyGroup 이 처리하므로 skip.
     */
    private fun refresh() {
        if (currentGroupId == null) return
        loadCalendar()
        // 복귀 시엔 이미 피드가 떠 있으므로 스켈레톤 없이 조용히 갱신(깜빡임 방지).
        loadFeeds(selectedDate, showLoading = false)
    }

    /** 그룹 선택 시트에서 다른 그룹 선택 → 현재 그룹 교체 후 재조회. */
    private fun selectGroup(groupId: Long) {
        val group = myGroups.firstOrNull { it.groupId == groupId } ?: return
        setState { copy(isGroupSheetVisible = false) }
        if (groupId == currentGroupId) return
        currentGroupId = groupId
        saveLastGroup(groupId)
        recordDates = emptyMap()
        setState {
            copy(
                groupName = group.name,
                groups = buildGroupUis(),
                feeds = emptyList(),
                weekDays = buildWeekDays(),
            )
        }
        loadCalendar()
        loadFeeds(selectedDate)
    }

    /** 마지막 선택 그룹 영속화. 실패해도 화면 동작엔 영향 없음(다음 진입에 복원만 안 될 뿐). */
    private fun saveLastGroup(groupId: Long) = viewModelScope.launch {
        runCatching { sessionRepository.saveLastGroupId(groupId) }
    }

    private fun buildGroupUis(): List<GroupUi> =
        myGroups.map {
            GroupUi(
                id = it.groupId,
                name = it.name,
                code = it.code,
                isCurrent = it.groupId == currentGroupId,
            )
        }

    /** 표시 중인 주의 기록 유무 캘린더 조회. */
    private fun loadCalendar() = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        runCatching { feedRepository.getActivityCalendar(groupId, weekStart) }
            .onSuccess { calendar ->
                recordDates = recordDates + calendar.days.associate { it.date to it.hasRecord }
                setState { copy(weekDays = buildWeekDays()) }
            }
    }

    private fun moveWeek(deltaWeeks: Long) {
        // 다음 주(미래)로는 이동 불가 — 이번 주가 상한.
        if (deltaWeeks > 0 && !canGoNextWeek()) return
        weekStart = weekStart.plusWeeks(deltaWeeks)
        setState {
            copy(
                weekLabel = formatWeekLabel(weekStart),
                weekDays = buildWeekDays(),
                canGoNextWeek = canGoNextWeek(),
            )
        }
        loadCalendar()
    }

    /** 이번 주 시작(일요일)보다 이전 주만 다음 주 이동 허용. */
    private fun canGoNextWeek(): Boolean = weekStart.isBefore(sundayOf(LocalDate.now()))

    /** 날짜 탭 연속 변경(주간 스와이프 등) 시 매 탭마다 조회하지 않도록 300ms 디바운스. */
    private fun selectDay(date: LocalDate) {
        selectedDate = date
        // 캘린더상 기록 없는(불 꺼진) 날: 스켈레톤 대신 빈 화면 즉시 + 백그라운드 조용히 조회.
        // 기록 있거나(true) 미상(null)인 날: 탭 즉시 스켈레톤(디바운스 대기 동안 이전 피드 안 남게).
        val hasRecord = recordDates[date] != false
        setState {
            copy(
                weekDays = buildWeekDays(),
                isLoading = hasRecord,
                feeds = if (hasRecord) feeds else emptyList(),
            )
        }
        selectDayJob?.cancel()
        selectDayJob = viewModelScope.launch {
            delay(300)
            loadFeedsSuspend(date, showLoading = hasRecord)
        }
    }

    /** 선택 날짜의 그룹 기록 목록 첫 페이지 조회 (즉시 호출용, 디바운스 없음). */
    private fun loadFeeds(date: LocalDate, showLoading: Boolean = true) = viewModelScope.launch {
        loadFeedsSuspend(date, showLoading)
    }

    /**
     * @param showLoading 스켈레톤 표시 여부. 캘린더상 기록 없는(불 꺼진) 날은 빈 화면을 즉시 보여주고
     *  백그라운드로 조용히 조회하기 위해 false.
     */
    private suspend fun loadFeedsSuspend(date: LocalDate, showLoading: Boolean = true) {
        val groupId = currentGroupId ?: return
        feedsCursor = null
        if (showLoading) setState { copy(isLoading = true) }
        runCatching { feedRepository.getRecords(groupId, date) }
            .onSuccess { page ->
                // 조회 중 날짜가 바뀌었으면(늦게 도착한 이전 날짜 응답) 무시 — 엉뚱한 날짜 결과 덮어쓰기 방지.
                if (date != selectedDate) return@onSuccess
                feedsCursor = page.nextCursor.takeIf { page.hasNext }
                setState {
                    copy(
                        feeds = page.records.map { it.toFeedCardUi() },
                        isLoading = false,
                        hasMoreFeeds = feedsCursor != null,
                    )
                }
            }
            .onFailure {
                if (date != selectedDate) return@onFailure
                // TODO(feed): 에러 노출 정책. 지금은 빈 목록.
                setState { copy(feeds = emptyList(), isLoading = false, hasMoreFeeds = false) }
            }
    }

    /** 신고 확인 다이얼로그 "신고하기" → 서버 접수 후 완료/실패 다이얼로그 전환. */
    private fun reportRecord() = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        val recordId = currentState.reportTargetRecordId ?: return@launch
        if (currentState.isReporting) return@launch
        setState { copy(isReporting = true) }
        runCatching { feedRepository.reportRecord(groupId, recordId) }
            .onSuccess {
                setState {
                    copy(isReporting = false, reportTargetRecordId = null, showReportComplete = true)
                }
            }
            .onFailure {
                setState {
                    copy(
                        isReporting = false,
                        reportTargetRecordId = null,
                        reportError = "신고 접수에 실패했어요. 다시 시도해주세요.",
                    )
                }
            }
    }

    /** 무한 스크롤: 다음 커서 페이지를 기존 목록에 이어붙임. */
    private fun loadMoreFeeds() = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        val cursor = feedsCursor ?: return@launch
        if (currentState.isLoadingMore) return@launch
        val date = selectedDate // 요청 시점 날짜 고정 — 조회 중 날짜 바뀌면 이어붙이기 취소.
        setState { copy(isLoadingMore = true) }
        runCatching { feedRepository.getRecords(groupId, date, cursor = cursor) }
            .onSuccess { page ->
                // 날짜가 바뀌었으면 다른 날짜 목록에 이어붙는 것 방지.
                if (date != selectedDate) return@onSuccess
                feedsCursor = page.nextCursor.takeIf { page.hasNext }
                setState {
                    copy(
                        feeds = feeds + page.records.map { it.toFeedCardUi() },
                        isLoadingMore = false,
                        hasMoreFeeds = feedsCursor != null,
                    )
                }
            }
            .onFailure {
                if (date != selectedDate) return@onFailure
                setState { copy(isLoadingMore = false) }
            }
    }

    private fun buildWeekDays(): List<WeekDayUi> {
        val today = LocalDate.now()
        return (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            WeekDayUi(
                date = date,
                weekdayLabel = WEEKDAY_LABELS[offset.toInt()],
                isSelected = date == selectedDate,
                hasFeed = recordDates[date] ?: false,
                isFuture = date.isAfter(today),
            )
        }
    }

    /** 해당 날짜가 속한 주(일요일 시작)의 일요일. */
    private fun sundayOf(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value % DayOfWeek.SUNDAY.value).toLong())

    /** "7월 2주차" — 주 시작 일요일이 그 달의 몇 번째 일요일인지. */
    private fun formatWeekLabel(sunday: LocalDate): String {
        val weekOrdinal = (sunday.dayOfMonth - 1) / 7 + 1
        return "${sunday.monthValue}월 ${weekOrdinal}주차"
    }

    private companion object {
        val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")
    }
}
