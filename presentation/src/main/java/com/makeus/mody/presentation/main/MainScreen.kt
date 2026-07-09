package com.makeus.mody.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
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
