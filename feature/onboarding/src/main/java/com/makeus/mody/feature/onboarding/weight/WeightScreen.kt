package com.makeus.mody.feature.onboarding.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
        title = "현재 체중과 목표 체중을\n입력해주세요",
        onNextClick = { viewModel.onIntent(OnboardingIntent.WeightNext) },
    ) {
        fun emit(current: Int, target: Int) =
            viewModel.onIntent(OnboardingIntent.WeightChanged(current, target))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            WeightColumn(
                label = "현재 체중",
                value = state.currentWeight,
                onChange = { emit(it, state.targetWeight) },
                modifier = Modifier.weight(1f),
            )
            WeightColumn(
                label = "목표 체중",
                value = state.targetWeight,
                onChange = { emit(state.currentWeight, it) },
                modifier = Modifier.weight(1f),
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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = ModyTheme.typography.b2,
            color = ModyTheme.colors.gray10,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        WheelPicker(
            items = WEIGHTS,
            selectedIndex = remember(value) { WEIGHTS.indexOf(value).coerceAtLeast(0) },
            onSelectedChange = { onChange(WEIGHTS[it]) },
            modifier = Modifier.fillMaxWidth(),
            unit = "kg",
            label = { "$it" },
        )
    }
}

@Composable
private fun WeightGuide(
    diff: Int,
    modifier: Modifier = Modifier,
) {
    if (diff == 0) return

    val amount = abs(diff)
    val verb = if (diff < 0) "감량" else "증량"
    val text = buildAnnotatedString {
        append("목표까지 ")
        withStyle(SpanStyle(color = ModyTheme.colors.gray10)) {
            append("${amount}kg")
        }
        append(" ${verb}이 필요해요!")
    }

    Text(
        text = text,
        style = ModyTheme.typography.c1,
        color = ModyTheme.colors.gray07,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
