package com.makeus.mody.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding(),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                MainTab.FEED -> FeedTabPlaceholder()
                MainTab.CHALLENGE -> ChallengeTabPlaceholder()
                MainTab.MY -> MyTab(viewModel)
            }
        }
        MainBottomBar(
            selected = selectedTab,
            onSelect = viewModel::selectTab,
        )
    }
}

// TODO(feed): 피드 화면 구현 시 교체.
@Composable
private fun FeedTabPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "피드",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray06,
        )
    }
}

// TODO(challenge): 챌린지 화면 구현 시 교체.
@Composable
private fun ChallengeTabPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "챌린지",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray06,
        )
    }
}

// TODO(my): 마이 화면 구현 시 교체. 지금은 개발용 임시 버튼 보관.
@Composable
private fun MyTab(viewModel: MainScreenViewModel) {
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        // TODO(temp): 개발 중 화면 이동 확인용. 플로우 완성 후 제거.
        ModyButton(
            text = "온보딩으로 이동",
            onClick = viewModel::goToOnboarding,
            variant = ModyButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        ModyButton(
            text = "그룹으로 이동",
            onClick = viewModel::goToGroup,
            variant = ModyButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        ModyButton(
            text = "로그아웃",
            onClick = viewModel::logout,
            variant = ModyButtonVariant.Dark,
            modifier = Modifier.fillMaxWidth(),
        )
        ModyButton(
            text = "회원탈퇴",
            onClick = { showWithdrawDialog = true },
            variant = ModyButtonVariant.Dark,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("회원탈퇴") },
            text = { Text("정말 탈퇴하시겠어요?\n계정과 데이터가 삭제되며 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(onClick = {
                    showWithdrawDialog = false
                    viewModel.withdraw()
                }) { Text("탈퇴") }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("취소") }
            },
        )
    }
}
