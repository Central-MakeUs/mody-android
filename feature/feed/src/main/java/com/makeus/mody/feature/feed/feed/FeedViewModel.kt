package com.makeus.mody.feature.feed.feed

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.FeedRecord
import com.makeus.mody.core.domain.model.RecordType
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.core.navigation.RecordGraph
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState
import com.makeus.mody.feature.feed.feed.contract.WeekDayUi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
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

    /** 현재 보고 있는 그룹. TODO(feed): 그룹 선택 시트에서 변경. */
    private var currentGroupId: Long? = null

    /** 날짜별 기록 유무 캐시 (주 이동 시 누적). */
    private var recordDates: Map<LocalDate, Boolean> = emptyMap()

    init {
        setState {
            copy(
                weekLabel = formatWeekLabel(weekStart),
                weekDays = buildWeekDays(),
            )
        }
        loadMyGroup()
    }

    override suspend fun processIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.PrevWeekClicked -> moveWeek(-1)
            is FeedIntent.NextWeekClicked -> moveWeek(1)
            is FeedIntent.DaySelected -> selectDay(intent.date)

            is FeedIntent.FabClicked -> setState { copy(isFabExpanded = !isFabExpanded) }
            is FeedIntent.FabDismissed -> setState { copy(isFabExpanded = false) }

            // TODO(feed): 각 액션 화면/API 연결
            is FeedIntent.GroupSelectorClicked -> Unit
            is FeedIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))
            is FeedIntent.PokeClicked -> Unit
            is FeedIntent.FeedCardClicked -> Unit
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
                val group = groups.firstOrNull() ?: return@onSuccess
                currentGroupId = group.groupId
                setState { copy(groupName = group.name) }
                loadCalendar()
                loadFeeds(selectedDate)
            }
        // 실패 시 그룹명/기록 점 미표시. TODO(feed): 에러 노출 정책 정해지면 처리.
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
        weekStart = weekStart.plusWeeks(deltaWeeks)
        setState {
            copy(
                weekLabel = formatWeekLabel(weekStart),
                weekDays = buildWeekDays(),
            )
        }
        loadCalendar()
    }

    private fun selectDay(date: LocalDate) {
        selectedDate = date
        setState { copy(weekDays = buildWeekDays()) }
        loadFeeds(date)
    }

    /** 선택 날짜의 그룹 기록 목록 조회. TODO(feed): 커서 페이지네이션(무한 스크롤). */
    private fun loadFeeds(date: LocalDate) = viewModelScope.launch {
        val groupId = currentGroupId ?: return@launch
        setState { copy(isLoading = true) }
        runCatching { feedRepository.getRecords(groupId, date) }
            .onSuccess { page ->
                setState { copy(feeds = page.records.map { it.toCardUi() }, isLoading = false) }
            }
            .onFailure {
                // TODO(feed): 에러 노출 정책. 지금은 빈 목록.
                setState { copy(feeds = emptyList(), isLoading = false) }
            }
    }

    private fun buildWeekDays(): List<WeekDayUi> =
        (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            WeekDayUi(
                date = date,
                weekdayLabel = WEEKDAY_LABELS[offset.toInt()],
                isSelected = date == selectedDate,
                hasFeed = recordDates[date] ?: false,
            )
        }

    /** 해당 날짜가 속한 주(일요일 시작)의 일요일. */
    private fun sundayOf(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value % DayOfWeek.SUNDAY.value).toLong())

    /** "7월 2주차" — 주 시작 일요일이 그 달의 몇 번째 일요일인지. */
    private fun formatWeekLabel(sunday: LocalDate): String {
        val weekOrdinal = (sunday.dayOfMonth - 1) / 7 + 1
        return "${sunday.monthValue}월 ${weekOrdinal}주차"
    }

    /** 도메인 기록 → 카드 표시 모델. 식사/운동에 따라 라벨·값 구성. */
    private fun FeedRecord.toCardUi(): FeedCardUi = when (type) {
        RecordType.MEAL -> FeedCardUi(
            id = recordId,
            authorName = nickname,
            dayCount = streakDays,
            primaryLabel = "식사 시간",
            primaryValue = recordedTime?.format(TIME_FORMAT).orEmpty(),
            secondaryLabel = "메뉴",
            secondaryValue = menu.orEmpty(),
            avatarUrl = profileImageUrl,
            imageUrl = imageUrl,
        )
        else -> FeedCardUi(
            id = recordId,
            authorName = nickname,
            dayCount = streakDays,
            primaryLabel = "운동 시간",
            primaryValue = exerciseDurationMinutes?.let { "${it}분" }.orEmpty(),
            secondaryLabel = "운동종류",
            secondaryValue = exerciseName.orEmpty(),
            avatarUrl = profileImageUrl,
            imageUrl = imageUrl,
        )
    }

    private companion object {
        val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
