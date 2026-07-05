package com.makeus.mody.feature.onboarding.alarm

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.component.WheelPicker
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
        fun emitMeal(b: Int?, l: Int?, d: Int?) =
            viewModel.onIntent(OnboardingIntent.MealHourChanged(b, l, d))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MealHourField(
                label = "아침 식사",
                hour = state.breakfastHour,
                defaultHour = 8,
                onPick = { emitMeal(it, state.lunchHour, state.dinnerHour) },
                onSkip = { emitMeal(null, state.lunchHour, state.dinnerHour) },
                modifier = Modifier.weight(1f),
            )
            MealHourField(
                label = "점심 식사",
                hour = state.lunchHour,
                defaultHour = 12,
                onPick = { emitMeal(state.breakfastHour, it, state.dinnerHour) },
                onSkip = { emitMeal(state.breakfastHour, null, state.dinnerHour) },
                modifier = Modifier.weight(1f),
            )
            MealHourField(
                label = "저녁 식사",
                hour = state.dinnerHour,
                defaultHour = 18,
                onPick = { emitMeal(state.breakfastHour, state.lunchHour, it) },
                onSkip = { emitMeal(state.breakfastHour, state.lunchHour, null) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "운동 일정",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                append("운동은 일주일에 ")
                withStyle(SpanStyle(color = ModyTheme.colors.secondary100)) {
                    append("최소 ${OnboardingState.EXERCISE_MIN_DAYS}번")
                }
                append("은 해야해요!")
            },
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        Spacer(modifier = Modifier.height(24.dp))

        // 선택 운동요일 공통 시각. 서버는 요일별 time 필요 → 이 값을 전 요일에 적용.
        var showExerciseSheet by remember { mutableStateOf(false) }
        Text(
            text = "운동 시간",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showExerciseSheet = true }
                .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatTime(state.exerciseHour, state.exerciseMinute),
                style = ModyTheme.typography.b4,
                color = ModyTheme.colors.gray10,
            )
            ChevronDown(tint = ModyTheme.colors.gray08)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )

        if (showExerciseSheet) {
            TimePickerSheet(
                initialHour = state.exerciseHour,
                initialMinute = state.exerciseMinute,
                onPick = { h, m ->
                    viewModel.onIntent(OnboardingIntent.ExerciseTimeChanged(h, m))
                    showExerciseSheet = false
                },
                onDismiss = { showExerciseSheet = false },
            )
        }
    }
}

/** 24h → "오전/오후 hh:mm" 표기. */
private fun formatTime(hour24: Int, minute: Int): String {
    val amPm = if (hour24 < 12) "오전" else "오후"
    val hour12 = ((hour24 + 11) % 12) + 1
    return "%s %02d:%02d".format(amPm, hour12, minute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealHourField(
    label: String,
    hour: Int?,
    defaultHour: Int,
    onPick: (Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val skipped = hour == null

    Column(modifier = modifier) {
        Text(
            text = label,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // "식사 안 함" 스킵 토글
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (skipped) onPick(defaultHour) else onSkip() },
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "식사 안 함",
                style = ModyTheme.typography.c1,
                color = if (skipped) ModyTheme.colors.gray10 else ModyTheme.colors.gray05,
            )
            Spacer(modifier = Modifier.size(4.dp))
            CheckCircle(checked = skipped)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 시각 필드 (밑줄형)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !skipped) { showSheet = true }
                .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = hour?.let { "%02d시".format(it) } ?: "--시",
                style = ModyTheme.typography.b4,
                color = if (skipped) ModyTheme.colors.gray04 else ModyTheme.colors.gray10,
            )
            ChevronDown(tint = if (skipped) ModyTheme.colors.gray04 else ModyTheme.colors.gray08)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
    }

    if (showSheet) {
        HourPickerSheet(
            initial = hour ?: defaultHour,
            onPick = {
                onPick(it)
                showSheet = false
            },
            onDismiss = { showSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourPickerSheet(
    initial: Int,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(initial) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ModyTheme.colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            WheelPicker(
                items = HOURS,
                selectedIndex = remember(selected) { HOURS.indexOf(selected).coerceAtLeast(0) },
                onSelectedChange = { selected = HOURS[it] },
                modifier = Modifier.fillMaxWidth(),
                label = { "%02d시".format(it) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ModyButton(
                text = "확인",
                onClick = { onPick(selected) },
                variant = ModyButtonVariant.Primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

private val AM_PM = listOf("오전", "오후")
private val HOURS_12 = (1..12).toList()
private val MINUTES = (0..59).toList()
private val SHEET_ITEM_HEIGHT = 40.dp

/** 오전/오후 · 시(1~12) · 분(0~59) 3열 휠. 공용 선택 바 위에 겹쳐 그림. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSheet(
    initialHour: Int, // 24h
    initialMinute: Int,
    onPick: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var amPmIndex by remember { mutableStateOf(if (initialHour < 12) 0 else 1) }
    var hour12 by remember { mutableStateOf(((initialHour + 11) % 12) + 1) }
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
            Box(contentAlignment = Alignment.Center) {
                // 공용 선택 바 (세 휠이 하나의 바를 공유)
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SHEET_ITEM_HEIGHT)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ModyTheme.colors.gray01),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WheelPicker(
                        items = AM_PM,
                        selectedIndex = amPmIndex,
                        onSelectedChange = { amPmIndex = it },
                        modifier = Modifier.weight(1f),
                        itemHeight = SHEET_ITEM_HEIGHT,
                        showSelectionBox = false,
                        label = { it },
                    )
                    WheelPicker(
                        items = HOURS_12,
                        selectedIndex = remember(hour12) {
                            HOURS_12.indexOf(hour12).coerceAtLeast(0)
                        },
                        onSelectedChange = { hour12 = HOURS_12[it] },
                        modifier = Modifier.weight(1f),
                        itemHeight = SHEET_ITEM_HEIGHT,
                        showSelectionBox = false,
                        label = { "$it" },
                    )
                    Text(
                        text = ":",
                        style = ModyTheme.typography.b1,
                        color = ModyTheme.colors.gray10,
                    )
                    WheelPicker(
                        items = MINUTES,
                        selectedIndex = remember(minute) {
                            MINUTES.indexOf(minute).coerceAtLeast(0)
                        },
                        onSelectedChange = { minute = MINUTES[it] },
                        modifier = Modifier.weight(1f),
                        itemHeight = SHEET_ITEM_HEIGHT,
                        showSelectionBox = false,
                        label = { "%02d".format(it) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ModyButton(
                text = "확인",
                onClick = { onPick(to24(amPmIndex, hour12), minute) },
                variant = ModyButtonVariant.Primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

/** (오전/오후, 12h) → 24h. 오전 12시=0시, 오후 12시=12시. */
private fun to24(amPmIndex: Int, hour12: Int): Int = when {
    amPmIndex == 0 && hour12 == 12 -> 0
    amPmIndex == 0 -> hour12
    hour12 == 12 -> 12
    else -> hour12 + 12
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
private fun CheckCircle(checked: Boolean) {
    val ring = ModyTheme.colors.gray03
    val fill = ModyTheme.colors.primary100
    val mark = ModyTheme.colors.gray10
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .then(
                if (checked) Modifier.background(fill)
                else Modifier.border(1.dp, ring, CircleShape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = mark,
                    start = Offset(w * 0.1f, h * 0.55f),
                    end = Offset(w * 0.4f, h * 0.85f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = mark,
                    start = Offset(w * 0.4f, h * 0.85f),
                    end = Offset(w * 0.9f, h * 0.2f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun ChevronDown(tint: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.42f),
            end = Offset(w * 0.5f, h * 0.62f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.62f),
            end = Offset(w * 0.7f, h * 0.42f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
