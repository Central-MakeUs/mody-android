package com.makeus.mody.feature.onboarding.nickname

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.OnboardingViewModel
import com.makeus.mody.feature.onboarding.component.OnboardingScaffold
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NicknameScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScaffold(
        stepIndex = 0,
        totalSteps = 4,
        title = "모디에서 불리고 싶은\n이름을 알려주세요",
        nextEnabled = state.isNicknameValid,
        onNextClick = { viewModel.onIntent(OnboardingIntent.NicknameNext) },
    ) {
        NicknameField(
            value = state.nickname,
            onValueChange = { viewModel.onIntent(OnboardingIntent.NicknameChanged(it)) },
        )
    }
}

@Composable
private fun ColumnScope.NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val overLimit = value.length > OnboardingState.NICKNAME_MAX
    val lineColor = if (overLimit) ModyTheme.colors.secondary100 else ModyTheme.colors.gray03

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = ModyTheme.typography.b2.copy(color = ModyTheme.colors.gray10),
        cursorBrush = SolidColor(ModyTheme.colors.primary100),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = "이름 또는 별명을 입력해주세요",
                    style = ModyTheme.typography.b2,
                    color = ModyTheme.colors.gray04,
                )
            }
            inner()
        },
    )

    Spacer(modifier = Modifier.height(8.dp))
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(lineColor),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (overLimit) "${OnboardingState.NICKNAME_MAX}자 이내로 적어주세요" else "",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.secondary100,
        )
        Text(
            text = "${value.length}/${OnboardingState.NICKNAME_MAX}",
            style = ModyTheme.typography.c1,
            color = if (overLimit) ModyTheme.colors.secondary100 else ModyTheme.colors.gray05,
        )
    }
}
