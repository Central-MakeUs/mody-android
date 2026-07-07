package com.makeus.mody.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
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
    }
}
