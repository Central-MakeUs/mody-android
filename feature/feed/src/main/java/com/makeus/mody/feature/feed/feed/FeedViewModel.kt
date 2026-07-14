package com.makeus.mody.feature.feed.feed

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.feed.feed.contract.CalendarDayUi
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    @Suppress("unused") private val navigationHelper: NavigationHelper, // TODO(feed): 화면 이동 연결 시 사용
) : BaseViewModel<FeedState, FeedIntent>(FeedState()) {

    private var calendarMonth: YearMonth = YearMonth.now()

    init {
        setState {
            copy(
                dateLabel = formatDate(LocalDate.now()),
                feeds = DUMMY_FEEDS, // TODO(feed): 피드 목록 API 연동
            )
        }
    }

    override suspend fun processIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.DateClicked -> openCalendar()
            is FeedIntent.CalendarDismissed -> setState { copy(isCalendarVisible = false) }
            is FeedIntent.CalendarPrevMonth -> moveMonth(-1)
            is FeedIntent.CalendarNextMonth -> moveMonth(1)
            // TODO(feed): 날짜 선택 → 해당 날짜 피드 조회
            is FeedIntent.CalendarDaySelected -> Unit
            is FeedIntent.CalendarConfirmClicked -> setState { copy(isCalendarVisible = false) }

            is FeedIntent.FabClicked -> setState { copy(isFabExpanded = !isFabExpanded) }
            is FeedIntent.FabDismissed -> setState { copy(isFabExpanded = false) }

            // TODO(feed): 각 액션 화면/API 연결
            is FeedIntent.MembersClicked -> Unit
            is FeedIntent.AlarmClicked -> Unit
            is FeedIntent.PokeClicked -> Unit
            is FeedIntent.FeedCardClicked -> Unit
            is FeedIntent.WriteExerciseClicked -> setState { copy(isFabExpanded = false) }
            is FeedIntent.WriteMealClicked -> setState { copy(isFabExpanded = false) }
        }
    }

    private fun openCalendar() {
        calendarMonth = YearMonth.now()
        setState {
            copy(
                isCalendarVisible = true,
                calendarTitle = formatMonth(calendarMonth),
                calendarDays = buildCalendarDays(calendarMonth),
            )
        }
    }

    private fun moveMonth(delta: Long) {
        calendarMonth = calendarMonth.plusMonths(delta)
        setState {
            copy(
                calendarTitle = formatMonth(calendarMonth),
                calendarDays = buildCalendarDays(calendarMonth),
            )
        }
    }

    /** 일요일 시작 그리드. 앞쪽 빈칸은 이전 달 생략(day=0 아님, inMonth=false 셀 미표시 대신 빈칸), 뒤쪽은 다음 달 날짜. */
    private fun buildCalendarDays(month: YearMonth): List<CalendarDayUi> {
        val today = LocalDate.now()
        val firstDay = month.atDay(1)
        val leadingBlanks = firstDay.dayOfWeek.value % 7 // 일=0, 월=1 ...
        val daysInMonth = month.lengthOfMonth()

        val cells = mutableListOf<CalendarDayUi>()
        repeat(leadingBlanks) { cells += CalendarDayUi(day = 0, inMonth = false, isToday = false, hasFeed = false) }
        for (day in 1..daysInMonth) {
            val date = month.atDay(day)
            cells += CalendarDayUi(
                day = day,
                inMonth = true,
                isToday = date == today,
                // TODO(feed): 피드 있는 날짜 API 연동. 지금은 더미(오늘 이전 날만 점 표시).
                hasFeed = date.isBefore(today),
            )
        }
        // 마지막 주 채우기: 다음 달 날짜(회색)
        var nextDay = 1
        while (cells.size % 7 != 0) {
            cells += CalendarDayUi(day = nextDay++, inMonth = false, isToday = false, hasFeed = false)
        }
        return cells
    }

    private fun formatDate(date: LocalDate): String = "${date.monthValue}월 ${date.dayOfMonth}일"

    private fun formatMonth(month: YearMonth): String =
        "%d.%02d".format(month.year, month.monthValue)

    private companion object {
        // TODO(feed): API 연동 전 확인용 더미 데이터.
        val DUMMY_FEEDS = listOf(
            FeedCardUi(
                id = 1,
                authorName = "난화영이다",
                dayCount = 2,
                primaryLabel = "식사 시간",
                primaryValue = "13:00",
                secondaryLabel = "메뉴",
                secondaryValue = "계란 3알, 사과 2조각",
            ),
            FeedCardUi(
                id = 2,
                authorName = "모나",
                dayCount = 5,
                primaryLabel = "운동 시간",
                primaryValue = "45분",
                secondaryLabel = "운동종류",
                secondaryValue = "런닝",
            ),
            FeedCardUi(
                id = 3,
                authorName = "난화영이다",
                dayCount = 2,
                primaryLabel = "운동 시간",
                primaryValue = "68분",
                secondaryLabel = "운동종류",
                secondaryValue = "필라테스",
            ),
        )
    }
}
