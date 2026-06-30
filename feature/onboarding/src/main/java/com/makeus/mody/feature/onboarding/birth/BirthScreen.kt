package com.makeus.mody.feature.onboarding.birth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.component.WheelPicker
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import java.time.LocalDate
import java.time.YearMonth

// 한국나이 14세 이상 → 출생연도 상한 = 올해 - 13 (매년 자동 반영)
private const val MIN_KOREAN_AGE = 14
private val MONTHS = (1..12).toList()

private fun maxBirthYear(): Int = LocalDate.now().year - (MIN_KOREAN_AGE - 1)

private fun daysIn(year: Int, month: Int): List<Int> =
    (1..YearMonth.of(year, month).lengthOfMonth()).toList()

@Composable
fun BirthScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val years = remember { (1950..maxBirthYear()).toList() }
    val days = remember(state.birthYear, state.birthMonth) {
        daysIn(state.birthYear, state.birthMonth)
    }

    OnboardingScaffold(
        stepIndex = 1,
        totalSteps = 4,
        title = "생년월일을 입력해주세요",
        subtitle = "한국나이로 14세 이상부터 사용할 수 있어요!",
        onNextClick = { viewModel.onIntent(OnboardingIntent.BirthNext) },
    ) {
        // 월/년 변경 시 존재하지 않는 날(예: 2월 31일)로 새는 것을 막기 위해 day 를 clamp
        fun emit(year: Int, month: Int, day: Int) {
            val clampedDay = day.coerceAtMost(YearMonth.of(year, month).lengthOfMonth())
            viewModel.onIntent(OnboardingIntent.BirthChanged(year, month, clampedDay))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WheelPicker(
                items = years,
                selectedIndex = remember(state.birthYear, years) { years.indexOf(state.birthYear).coerceAtLeast(0) },
                onSelectedChange = { emit(years[it], state.birthMonth, state.birthDay) },
                modifier = Modifier.weight(2f),
                label = { "$it" },
            )
            WheelPicker(
                items = MONTHS,
                selectedIndex = remember(state.birthMonth) { MONTHS.indexOf(state.birthMonth).coerceAtLeast(0) },
                onSelectedChange = { emit(state.birthYear, MONTHS[it], state.birthDay) },
                modifier = Modifier.weight(1f),
                label = { "%02d".format(it) },
            )
            WheelPicker(
                items = days,
                selectedIndex = remember(state.birthDay, days) { days.indexOf(state.birthDay).coerceAtLeast(0) },
                onSelectedChange = { emit(state.birthYear, state.birthMonth, days[it]) },
                modifier = Modifier.weight(1f),
                label = { "%02d".format(it) },
            )
        }
    }
}
