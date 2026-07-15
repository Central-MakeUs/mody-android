package com.makeus.mody.feature.onboarding.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.core.designsystem.component.ModyTimePicker
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState
import com.makeus.mody.feature.onboarding.contract.TimeOfDay

private val DAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")
private val BREAKFAST_HOURS = (7..11).toList()
private val LUNCH_HOURS = (12..16).toList()
private val DINNER_HOURS = (17..21).toList()

/** 운동 시간 모달 대상. */
private sealed interface ExerciseModal {
    data class Day(val day: Int, val initial: TimeOfDay) : ExerciseModal
    data object All : ExerciseModal
}

@Composable
fun AlarmScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var exerciseModal by remember { mutableStateOf<ExerciseModal?>(null) }

    OnboardingScaffold(
        stepIndex = 3,
        totalSteps = 4,
        title = "식사와 운동 알림을\n언제 드릴까요?",
        nextEnabled = state.isExerciseValid,
        onNextClick = { viewModel.onIntent(OnboardingIntent.AlarmNext) },
    ) {
        fun emitMeal(b: Int?, l: Int?, d: Int?) =
            viewModel.onIntent(OnboardingIntent.MealHourChanged(b, l, d))

        // 식사 필드 3열. 각 필드 탭 → 아래로 시간 선택박스(DropdownMenu)가 덮어서 열림.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MealColumn(
                label = "아침 식사",
                hour = state.breakfastHour,
                defaultHour = 8,
                hours = BREAKFAST_HOURS,
                onPick = { emitMeal(it, state.lunchHour, state.dinnerHour) },
                onToggleSkip = {
                    if (state.breakfastHour == null) emitMeal(8, state.lunchHour, state.dinnerHour)
                    else emitMeal(null, state.lunchHour, state.dinnerHour)
                },
            )
            MealColumn(
                label = "점심 식사",
                hour = state.lunchHour,
                defaultHour = 12,
                hours = LUNCH_HOURS,
                onPick = { emitMeal(state.breakfastHour, it, state.dinnerHour) },
                onToggleSkip = {
                    if (state.lunchHour == null) emitMeal(state.breakfastHour, 12, state.dinnerHour)
                    else emitMeal(state.breakfastHour, null, state.dinnerHour)
                },
            )
            MealColumn(
                label = "저녁 식사",
                hour = state.dinnerHour,
                defaultHour = 18,
                hours = DINNER_HOURS,
                onPick = { emitMeal(state.breakfastHour, state.lunchHour, it) },
                onToggleSkip = {
                    if (state.dinnerHour == null) emitMeal(state.breakfastHour, state.lunchHour, 18)
                    else emitMeal(state.breakfastHour, state.lunchHour, null)
                },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "운동 일정",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                append("운동은 일주일에 ")
                withStyle(SpanStyle(color = ModyTheme.colors.secondary100)) {
                    append("최소 ${nativeKoreanNumber(OnboardingState.EXERCISE_MIN_DAYS)} 번")
                }
                append("은 해야해요!")
            },
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 요일 칩: 미선택 탭 → 모달로 시간 지정 후 선택 / 선택된 칩 탭 → 해제
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DAY_LABELS.forEachIndexed { index, label ->
                val day = index + 1
                DayChip(
                    label = label,
                    selected = day in state.exerciseDays,
                    onClick = {
                        if (day in state.exerciseDays) {
                            viewModel.onIntent(OnboardingIntent.ExerciseDayRemoved(day))
                        } else {
                            // 요일 선택 시 기본 오전 9:00로 바로 지정(모달 없이). 시간 변경은 아래 시간 행 탭.
                            viewModel.onIntent(OnboardingIntent.ExerciseDaySet(day, 9, 0))
                        }
                    },
                )
            }
        }

        // 선택된 요일별 시간 행 + "모두 같은 시간으로 설정"
        val sortedDays = state.exerciseTimes.toSortedMap()
        if (sortedDays.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            sortedDays.entries.forEachIndexed { index, (day, time) ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                ExerciseTimeRow(
                    label = DAY_LABELS[day - 1],
                    time = formatTime(time.hour, time.minute),
                    onClick = { exerciseModal = ExerciseModal.Day(day, time) },
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "모두 같은 시간으로 설정",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray09,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { exerciseModal = ExerciseModal.All },
            )
        }
    }

    exerciseModal?.let { modal ->
        val initial = when (modal) {
            is ExerciseModal.Day -> modal.initial
            ExerciseModal.All -> state.exerciseTimes.values.firstOrNull() ?: TimeOfDay(9, 0)
        }
        TimePickerSheet(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            onPick = { h, m ->
                when (modal) {
                    is ExerciseModal.Day -> viewModel.onIntent(OnboardingIntent.ExerciseDaySet(modal.day, h, m))
                    ExerciseModal.All -> viewModel.onIntent(OnboardingIntent.ExerciseAllTimesSet(h, m))
                }
                exerciseModal = null
            },
            onDismiss = { exerciseModal = null },
        )
    }
}

/** 24h → "오전/오후 h:mm" 표기(운동 행). */
private fun formatTime(hour24: Int, minute: Int): String {
    val amPm = if (hour24 < 12) "오전" else "오후"
    val hour12 = ((hour24 + 11) % 12) + 1
    return "%s %d:%02d".format(amPm, hour12, minute)
}

// 고유어 수사(한/두/세/네/다섯/여섯/일곱). 범위 밖이면 숫자 그대로.
private val NATIVE_KOREAN_NUMBERS = listOf("한", "두", "세", "네", "다섯", "여섯", "일곱")

private fun nativeKoreanNumber(n: Int): String =
    NATIVE_KOREAN_NUMBERS.getOrElse(n - 1) { n.toString() }

@Composable
private fun RowScope.MealColumn(
    label: String,
    hour: Int?,
    defaultHour: Int,
    hours: List<Int>,
    onPick: (Int) -> Unit,
    onToggleSkip: () -> Unit,
) {
    val skipped = hour == null
    var expanded by remember { mutableStateOf(false) }
    val selected = hour ?: defaultHour

    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // "식사 안 함" 좌측정렬 + 체크(체크 시 골드)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleSkip() },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "식사 안 함",
                style = ModyTheme.typography.c1.copy(fontWeight = FontWeight.SemiBold),
                color = if (skipped) ModyTheme.colors.gray10 else ModyTheme.colors.gray05,
            )
            Spacer(modifier = Modifier.size(4.dp))
            CheckMark(checked = skipped)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 시각 필드 (밑줄형). 탭 → 아래로 시간 선택박스가 덮어서 열림.
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !skipped) { expanded = true }
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "%02d시".format(selected),
                    style = ModyTheme.typography.b4,
                    color = if (skipped) ModyTheme.colors.gray04 else ModyTheme.colors.gray10,
                )
                Icon(
                    painter = painterResource(if (expanded) ModyIcons.Up else ModyIcons.Down),
                    contentDescription = null,
                    tint = if (expanded) ModyTheme.colors.gray09 else ModyTheme.colors.gray03,
                    modifier = Modifier.size(20.dp),
                )
            }

            DropdownMenu(
                expanded = expanded && !skipped,
                onDismissRequest = { expanded = false },
                containerColor = ModyTheme.colors.white,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(max = 240.dp),
            ) {
                hours.forEach { h ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "%02d시".format(h),
                                style = ModyTheme.typography.b4,
                                color = if (h == selected) ModyTheme.colors.gray10 else ModyTheme.colors.gray06,
                            )
                        },
                        onClick = {
                            onPick(h)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (h == selected) ModyTheme.colors.primary400 else Color.Transparent,
                        ),
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
    }
}

@Composable
private fun ExerciseTimeRow(
    label: String,
    time: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.gray01)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.black,
        )
        Text(
            text = time,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray10,
        )
    }
}

/** 오전/오후 · 시(1~12) · 분(0~59) 3열 휠 시트. 휠 UI는 공용 ModyTimePicker 사용. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSheet(
    initialHour: Int, // 24h
    initialMinute: Int,
    onPick: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hour24 by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ModyTheme.colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            ModyTimePicker(
                hour24 = hour24,
                minute = minute,
                onTimeChange = { h, m ->
                    hour24 = h
                    minute = m
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ModyButton(
                text = "확인",
                onClick = { onPick(hour24, minute) },
                variant = ModyButtonVariant.Primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
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
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) ModyTheme.colors.primary100 else ModyTheme.colors.gray01)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b7,
            color = if (selected) ModyTheme.colors.gray10 else ModyTheme.colors.gray06,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CheckMark(checked: Boolean) {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = if (checked) ModyTheme.colors.primary100 else ModyTheme.colors.gray04,
        modifier = Modifier.size(16.dp),
    )
}

