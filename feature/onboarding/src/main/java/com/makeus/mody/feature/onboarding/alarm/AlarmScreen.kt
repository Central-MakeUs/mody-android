package com.makeus.mody.feature.onboarding.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState

private val DAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")
private val HOURS = (0..23).toList()

@Composable
fun AlarmScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScaffold(
        stepIndex = 3,
        totalSteps = 4,
        title = "식사와 운동 알림을\n언제 드릴까요?",
        nextEnabled = state.isExerciseValid,
        onNextClick = { viewModel.onIntent(OnboardingIntent.AlarmNext) },
    ) {
        fun emitMeal(b: Int, l: Int, d: Int) =
            viewModel.onIntent(OnboardingIntent.MealHourChanged(b, l, d))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MealHourField(
                label = "아침 식사",
                hour = state.breakfastHour,
                onHourChange = { emitMeal(it, state.lunchHour, state.dinnerHour) },
                modifier = Modifier.weight(1f),
            )
            MealHourField(
                label = "점심 식사",
                hour = state.lunchHour,
                onHourChange = { emitMeal(state.breakfastHour, it, state.dinnerHour) },
                modifier = Modifier.weight(1f),
            )
            MealHourField(
                label = "저녁 식사",
                hour = state.dinnerHour,
                onHourChange = { emitMeal(state.breakfastHour, state.lunchHour, it) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "운동 일정",
            style = ModyTheme.typography.b4,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "운동은 일주일에 최소 ${OnboardingState.EXERCISE_MIN_DAYS}번은 해야해요!",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DAY_LABELS.forEachIndexed { index, label ->
                val day = index + 1
                DayChip(
                    label = label,
                    selected = day in state.exerciseDays,
                    onClick = { viewModel.onIntent(OnboardingIntent.ExerciseDayToggled(day)) },
                )
            }
        }
    }
}

@Composable
private fun MealHourField(
    label: String,
    hour: Int,
    onHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray08,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, ModyTheme.colors.gray03, RoundedCornerShape(10.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "%02d시".format(hour),
                    style = ModyTheme.typography.b4,
                    color = ModyTheme.colors.gray10,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                HOURS.forEach { h ->
                    DropdownMenuItem(
                        text = { Text("%02d시".format(h)) },
                        onClick = {
                            onHourChange(h)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selected) ModyTheme.colors.primary100 else ModyTheme.colors.gray02)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b4,
            color = if (selected) ModyTheme.colors.gray10 else ModyTheme.colors.gray06,
            textAlign = TextAlign.Center,
        )
    }
}
