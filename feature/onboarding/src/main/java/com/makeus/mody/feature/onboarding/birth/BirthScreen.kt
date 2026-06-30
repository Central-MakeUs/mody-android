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

@Composable
fun BirthScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val years = remember { BirthDateOptions.years() }
    val days = remember(state.birthYear, state.birthMonth) {
        BirthDateOptions.days(state.birthYear, state.birthMonth)
    }

    OnboardingScaffold(
        stepIndex = 1,
        totalSteps = 4,
        title = "생년월일을 입력해주세요",
        subtitle = "한국나이로 14세 이상부터 사용할 수 있어요!",
        onNextClick = { viewModel.onIntent(OnboardingIntent.BirthNext) },
    ) {
        fun emit(year: Int, month: Int, day: Int) {
            val clampedDay = BirthDateOptions.clampDay(year, month, day)
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
                items = BirthDateOptions.months,
                selectedIndex = remember(state.birthMonth) {
                    BirthDateOptions.months.indexOf(state.birthMonth).coerceAtLeast(0)
                },
                onSelectedChange = { emit(state.birthYear, BirthDateOptions.months[it], state.birthDay) },
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
