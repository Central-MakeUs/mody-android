package com.makeus.mody.feature.feed.feed

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.domain.repository.GroupRepository
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
) : BaseViewModel<FeedState, FeedIntent>(FeedState()) {

    /** 현재 선택된 날짜 (피드 조회 기준). */
    private var selectedDate: LocalDate = LocalDate.now()

    /** 주간 스트립에 표시 중인 주의 일요일. */
    private var weekStart: LocalDate = sundayOf(selectedDate)

    /** 현재 보고 있는 그룹. TODO(feed): 마지막 선택 그룹 기억. */
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

    /** 내 그룹 조회 → 첫 그룹을 현재 그룹으로. TODO(feed): 마지막 선택 그룹 기억. */
    private fun loadMyGroup() = viewModelScope.launch {
        runCatching { groupRepository.getMyGroups() }
            .onSuccess { groups ->
                myGroups = groups
                val group = groups.firstOrNull() ?: return@onSuccess
                currentGroupId = group.groupId
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
        loadFeeds(selectedDate)
    }

    /** 그룹 선택 시트에서 다른 그룹 선택 → 현재 그룹 교체 후 재조회. */
    private fun selectGroup(groupId: Long) {
        val group = myGroups.firstOrNull { it.groupId == groupId } ?: return
        setState { copy(isGroupSheetVisible = false) }
        if (groupId == currentGroupId) return
        currentGroupId = groupId
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
        setState { copy(weekDays = buildWeekDays()) }
        selectDayJob?.cancel()
        selectDayJob = viewModelScope.launch {
            delay(300)
            loadFeedsSuspend(date)
        }
    }

    /** 선택 날짜의 그룹 기록 목록 첫 페이지 조회 (즉시 호출용, 디바운스 없음). */
    private fun loadFeeds(date: LocalDate) = viewModelScope.launch {
        loadFeedsSuspend(date)
    }

    private suspend fun loadFeedsSuspend(date: LocalDate) {
        val groupId = currentGroupId ?: return
        feedsCursor = null
        setState { copy(isLoading = true) }
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
