package com.makeus.mody.feature.onboarding.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.component.WheelPicker
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import kotlin.math.abs

private val WEIGHTS = (30..200).toList()

@Composable
fun WeightScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScaffold(
        stepIndex = 2,
        totalSteps = 4,
        title = "체중을 입력해주세요",
        subtitle = "현재 체중과 목표 체중이 필요해요.",
        onNextClick = { viewModel.onIntent(OnboardingIntent.WeightNext) },
    ) {
        fun emit(current: Int, target: Int) =
            viewModel.onIntent(OnboardingIntent.WeightChanged(current, target))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        ) {
            WeightColumn(
                label = "현재 체중",
                value = state.currentWeight,
                onChange = { emit(it, state.targetWeight) },
            )
            WeightColumn(
                label = "목표 체중",
                value = state.targetWeight,
                onChange = { emit(state.currentWeight, it) },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        WeightGuide(
            diff = state.weightDiff,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WeightColumn(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray08,
            modifier = Modifier.padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 왼쪽에 오른쪽 kg와 동일 폭의 투명 balancer → 선택박스가 구조적으로 정중앙
            KgUnit(modifier = Modifier.alpha(0f))
            Spacer(modifier = Modifier.width(4.dp))
            WheelPicker(
                items = WEIGHTS,
                selectedIndex = remember(value) { WEIGHTS.indexOf(value).coerceAtLeast(0) },
                onSelectedChange = { onChange(WEIGHTS[it]) },
                itemHeight = 36.dp,
                fillItemWidth = false,
                itemHorizontalPadding = 26.dp,
                label = { "$it" },
            )
            Spacer(modifier = Modifier.width(4.dp))
            KgUnit()
        }
    }
}

@Composable
private fun KgUnit(modifier: Modifier = Modifier) {
    Text(
        text = "kg",
        style = ModyTheme.typography.b7,
        color = ModyTheme.colors.gray04,
        modifier = modifier,
    )
}

@Composable
private fun WeightGuide(
    diff: Int,
    modifier: Modifier = Modifier,
) {
    val text = if (diff == 0) {
        buildAnnotatedString { append("현재 목표 체중을 유지하고 있어요!") }
    } else {
        val amount = abs(diff)
        val verb = if (diff < 0) "감량" else "증량"
        buildAnnotatedString {
            append("목표까지 ")
            withStyle(
                SpanStyle(
                    color = ModyTheme.colors.secondary100,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                ),
            ) {
                append("${amount}kg")
            }
            append(" ${verb}이 필요해요!")
        }
    }

    Text(
        text = text,
        style = ModyTheme.typography.c1,
        color = ModyTheme.colors.gray07,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
