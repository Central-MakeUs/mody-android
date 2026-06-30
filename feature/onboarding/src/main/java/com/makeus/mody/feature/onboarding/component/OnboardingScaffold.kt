package com.makeus.mody.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 온보딩 전 스텝 공용 골격.
 * 상단 진행바 + 타이틀(+서브) + 가변 content + 하단 "다음으로" 버튼.
 */
@Composable
fun OnboardingScaffold(
    stepIndex: Int,
    totalSteps: Int,
    title: String,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    nextEnabled: Boolean = true,
    nextText: String = "다음으로",
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        StepProgressBar(
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            modifier = Modifier.padding(top = 12.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = title,
            style = ModyTheme.typography.h2,
            color = ModyTheme.colors.gray10,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray06,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        content()

        Spacer(modifier = Modifier.weight(1f))

        ModyButton(
            text = nextText,
            onClick = onNextClick,
            variant = ModyButtonVariant.Primary,
            enabled = nextEnabled,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun StepProgressBar(
    stepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSteps) { i ->
            val active = i == stepIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) ModyTheme.colors.primary100 else ModyTheme.colors.gray02
                    )
            ) {}
        }
    }
}
