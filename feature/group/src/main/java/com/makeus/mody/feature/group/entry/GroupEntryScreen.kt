package com.makeus.mody.feature.group.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.group.GroupViewModel
import com.makeus.mody.feature.group.component.GroupScaffold
import com.makeus.mody.feature.group.component.HighlightGold
import com.makeus.mody.feature.group.contract.GroupIntent
import com.makeus.mody.feature.group.contract.GroupState
import com.makeus.mody.feature.group.contract.JoinCodeError

@Composable
fun GroupEntryScreen(viewModel: GroupViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GroupScaffold(
        title = "회원가입 완료!",
        subtitle = buildAnnotatedString {
            append("이제 친구들과 함께, ")
            withStyle(SpanStyle(color = HighlightGold, fontWeight = FontWeight.Bold)) { append("모디") }
            append("에서\n건강한 다이어트 습관을 길러보세요!")
        },
    ) {
        JoinCodeField(
            value = state.joinCode,
            error = state.joinError,
            onValueChange = { viewModel.onIntent(GroupIntent.JoinCodeChanged(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 참여 요청 진행중이면 스피너, 아니면 버튼
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ModyTheme.colors.primary100)
            }
        } else {
            ModyButton(
                text = "그룹 참여하기",
                onClick = { viewModel.onIntent(GroupIntent.JoinClicked) },
                variant = ModyButtonVariant.Primary,
                enabled = state.isJoinEnabled,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "초대받은 그룹이 없나요?",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray07,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        ModyButton(
            text = "새로운 그룹 만들기",
            onClick = { viewModel.onIntent(GroupIntent.CreateGroupClicked) },
            variant = ModyButtonVariant.Dark,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ColumnScope.JoinCodeField(
    value: String,
    error: JoinCodeError?,
    onValueChange: (String) -> Unit,
) {
    val lineColor = if (error != null) ModyTheme.colors.error else ModyTheme.colors.gray02

    ModyTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "코드를 입력해주세요",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        alertIcon = if (error != null) R.drawable.ic_alert_filled else null,
        trailingIcon = if (value.isNotEmpty()) R.drawable.ic_clear else null,
        onTrailingIconClick = { onValueChange("") },
        trailingIconContentDescription = "입력 지우기",
        maxLength = GroupState.JOIN_CODE_LENGTH,
        // 코드는 영문+숫자(대문자) → 영문 키패드로 바로 뜨게
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Characters,
        ),
    )

    Spacer(modifier = Modifier.height(8.dp))
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(lineColor),
    )

    if (error != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (error) {
                JoinCodeError.NOT_FOUND -> "존재하지 않는 코드입니다."
                JoinCodeError.FULL -> "이미 인원이 꽉 찬 그룹이에요."
            },
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.error,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
