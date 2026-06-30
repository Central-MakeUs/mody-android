package com.makeus.mody.feature.onboarding.nickname

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyTextField
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
    val lineColor = if (value.length == OnboardingState.NICKNAME_MAX) ModyTheme.colors.error else ModyTheme.colors.gray02

    val isAtLimit = value.length == OnboardingState.NICKNAME_MAX
    val showClearIcon = value.isNotEmpty()
    val showAlertIcon = isAtLimit

    ModyTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "이름 또는 별명을 입력해주세요",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        alertIcon = if (showAlertIcon) R.drawable.ic_alert_filled else null,
        trailingIcon = if (showClearIcon) R.drawable.ic_clear else null,
        onTrailingIconClick = { onValueChange("") },
        maxLength = OnboardingState.NICKNAME_MAX,
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
            .padding(top = 6.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (isAtLimit) "${OnboardingState.NICKNAME_MAX}자 이내로 입력해주세요" else "",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.error,
        )
        val countText = buildAnnotatedString {
            val limitStr = "/${OnboardingState.NICKNAME_MAX}"
            val beforeLimit = value.length.toString()
            
            append(beforeLimit)
            append(limitStr)
            
            if (isAtLimit) {
                addStyle(SpanStyle(color = ModyTheme.colors.error), 0, beforeLimit.length)
            }
        }
        Text(
            text = countText,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray07,
        )
    }
}
