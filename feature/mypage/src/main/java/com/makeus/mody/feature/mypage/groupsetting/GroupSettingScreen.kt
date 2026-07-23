package com.makeus.mody.feature.mypage.groupsetting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.feature.mypage.groupsetting.contract.GroupSettingIntent
import com.makeus.mody.feature.mypage.groupsetting.contract.GroupSettingState

@Composable
fun GroupSettingScreen(viewModel: GroupSettingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GroupSettingContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun GroupSettingContent(
    state: GroupSettingState,
    onIntent: (GroupSettingIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white),
    ) {
        ModyBackTopBar(
            title = "그룹 설정",
            onBackClick = { onIntent(GroupSettingIntent.BackClicked) },
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.groups, key = { it.groupId }) { group ->
                GroupRow(
                    group = group,
                    onLeaveClick = { onIntent(GroupSettingIntent.LeaveClicked(group)) },
                )
            }
        }
    }

    state.leaveTarget?.let {
        ModyDialog(
            title = "정말 그룹에서 나가실건가요?",
            message = "그룹에서 나가면 모든 정보가 사라져요.",
            dismissText = "취소",
            confirmText = "그룹 나가기",
            onConfirm = { onIntent(GroupSettingIntent.LeaveConfirmed) },
            onDismissRequest = { onIntent(GroupSettingIntent.LeaveDismissed) },
        )
    }

    ModyErrorDialog(
        message = state.errorMessage,
        onDismiss = { onIntent(GroupSettingIntent.ErrorShown) },
    )
}

@Composable
private fun GroupRow(
    group: Group,
    onLeaveClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = group.name,
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "그룹",
                    style = ModyTheme.typography.b7,
                    color = ModyTheme.colors.gray05,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ModyTheme.colors.gray02)
                    .clickable(onClick = onLeaveClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "그룹 나가기",
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray05,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray01),
        )
    }
}
