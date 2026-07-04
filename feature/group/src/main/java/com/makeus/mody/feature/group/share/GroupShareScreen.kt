package com.makeus.mody.feature.group.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.group.GroupViewModel
import com.makeus.mody.feature.group.component.GroupScaffold
import com.makeus.mody.feature.group.contract.GroupIntent

@Composable
fun GroupShareScreen(viewModel: GroupViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    GroupScaffold(
        title = "그룹에 함께할 친구를\n초대해보세요!",
        subtitle = buildAnnotatedString { append("코드를 클릭해 복사하거나 카카오톡으로 공유하세요.") },
        imeAware = false,
    ) {
        InviteCodeRow(
            code = state.inviteCode,
            onCopyClick = {
                val code = state.inviteCode ?: return@InviteCodeRow
                clipboard.setText(AnnotatedString(code))
                viewModel.onIntent(GroupIntent.CopyCodeClicked)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        ModyButton(
            text = "카카오톡으로 공유하기",
            onClick = { viewModel.onIntent(GroupIntent.KakaoShareClicked) },
            variant = ModyButtonVariant.Dark,
        )

        if (state.codeCopied) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "코드가 복사되었어요.",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray06,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ModyButton(
            text = "완료",
            onClick = { viewModel.onIntent(GroupIntent.ShareDoneClicked) },
            variant = ModyButtonVariant.Primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ColumnScope.InviteCodeRow(
    code: String?,
    onCopyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "코드 복사",
                role = Role.Button,
                onClick = onCopyClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // code == null 이면 로딩중 → "• • •"
            Text(
                text = code ?: "• • •",
                style = ModyTheme.typography.b4,
                color = if (code != null) ModyTheme.colors.gray10 else ModyTheme.colors.gray04,
            )
            Text(
                text = "코드 복사",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray06,
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
    }
}
