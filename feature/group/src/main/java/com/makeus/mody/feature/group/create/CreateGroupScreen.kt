package com.makeus.mody.feature.group.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyInputFilter
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.group.GroupViewModel
import com.makeus.mody.feature.group.component.GroupScaffold
import com.makeus.mody.feature.group.component.HighlightGold
import com.makeus.mody.feature.group.contract.GroupIntent
import com.makeus.mody.feature.group.contract.GroupState

@Composable
fun CreateGroupScreen(viewModel: GroupViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 생성 실패(GROUP304 등) → 토스트 1회 표시 후 상태 소비
    LaunchedEffect(state.createError) {
        state.createError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(GroupIntent.CreateErrorShown)
        }
    }

    GroupScaffold(
        title = "그룹 이름을 정해볼까요?",
        subtitle = buildAnnotatedString {
            append("친구들과 함께할 그룹의 이름이에요.\n")
            withStyle(SpanStyle(color = HighlightGold, fontWeight = FontWeight.Bold)) {
                append("최대 ${GroupState.MAX_MEMBERS}명")
            }
            append("까지 초대할 수 있어요!")
        },
        onBackClick = { viewModel.onIntent(GroupIntent.BackClicked) },
    ) {
        GroupNameField(
            value = state.groupName,
            onValueChange = { viewModel.onIntent(GroupIntent.GroupNameChanged(it)) },
        )

        Spacer(modifier = Modifier.weight(1f))

        ModyButton(
            text = "다음으로",
            onClick = { viewModel.onIntent(GroupIntent.GroupNameNext) },
            variant = ModyButtonVariant.Primary,
            enabled = state.isGroupNameValid,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ColumnScope.GroupNameField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    // 최대 글자수는 유효값(GROUP_NAME_MAX). 한 글자 더(초과) 입력되면 경고 표시 → 다음 버튼은 비활성.
    val isOverLimit = value.length > GroupState.GROUP_NAME_MAX
    val lineColor = if (isOverLimit) ModyTheme.colors.error else ModyTheme.colors.gray02

    ModyTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "이름 또는 별명을 입력해주세요",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        alertIcon = if (isOverLimit) R.drawable.ic_alert_filled else null,
        trailingIcon = if (value.isNotEmpty()) R.drawable.ic_clear else null,
        onTrailingIconClick = { onValueChange("") },
        trailingIconContentDescription = "입력 지우기",
        maxLength = GroupState.GROUP_NAME_MAX + 1,
        inputFilter = ModyInputFilter::hangulAlphaNumeric,
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
            text = if (isOverLimit) "${GroupState.GROUP_NAME_MAX}자 이내로 입력해주세요" else "",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.error,
        )
        val countText = buildAnnotatedString {
            val before = value.length.toString()
            append(before)
            append("/${GroupState.GROUP_NAME_MAX}")
            if (isOverLimit) addStyle(SpanStyle(color = ModyTheme.colors.error), 0, before.length)
        }
        Text(
            text = countText,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray07,
        )
    }
}
