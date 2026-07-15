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
import com.makeus.mody.core.designsystem.component.ModyInputFilter
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.navigation.GroupEntrySource
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.group.GroupViewModel
import com.makeus.mody.feature.group.component.GroupScaffold
import com.makeus.mody.feature.group.component.HighlightGold
import com.makeus.mody.feature.group.contract.GroupIntent
import com.makeus.mody.feature.group.contract.GroupState

@Composable
fun GroupEntryScreen(
    viewModel: GroupViewModel,
    // 진입 출처. 소스별로 title/subtitle/뒤로가기 노출 분기.
    source: GroupEntrySource = GroupEntrySource.Onboarding,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val title = when (source) {
        GroupEntrySource.Onboarding -> "회원가입 완료!"
        GroupEntrySource.Feed -> "그룹 참여하기"
    }
    val subtitle = when (source) {
        GroupEntrySource.Onboarding -> buildAnnotatedString {
            append("이제 친구들과 함께, ")
            withStyle(SpanStyle(color = HighlightGold, fontWeight = FontWeight.Bold)) { append("모디") }
            append("에서\n건강한 다이어트 습관을 길러보세요!")
        }
        GroupEntrySource.Feed -> buildAnnotatedString {
            append("친구들과 함께, ")
            withStyle(SpanStyle(color = HighlightGold, fontWeight = FontWeight.Bold)) { append("모디") }
            append("에서\n건강한 다이어트 습관을 길러보세요!")
        }
    }
    // 온보딩=플로우 시작점(뒤로가기 없음). 피드=복귀용 뒤로가기.
    val showBack = when (source) {
        GroupEntrySource.Onboarding -> false
        GroupEntrySource.Feed -> true
    }

    GroupScaffold(
        title = title,
        subtitle = subtitle,
        onBackClick = if (showBack) {
            { viewModel.onIntent(GroupIntent.BackClicked) }
        } else {
            null
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
                    .height(48.dp), // ModyButton 높이와 동일 → 로딩/idle 전환 시 점프 없음
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

        // 참여 실패 사유: 버튼 아래 좌측정렬(scaffold 좌우 24 그대로)
        state.joinError?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.error,
                modifier = Modifier.fillMaxWidth(),
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
    error: String?,
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
        // 이모지/특수문자/한글 차단 → 영대문자+숫자만
        inputFilter = ModyInputFilter::upperAlphaNumeric,
    )

    Spacer(modifier = Modifier.height(8.dp))
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(lineColor),
    )
}
