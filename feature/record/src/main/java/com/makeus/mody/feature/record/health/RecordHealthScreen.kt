package com.makeus.mody.feature.record.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.record.component.RecordTopBar
import com.makeus.mody.feature.record.health.contract.RecordHealthIntent

// TODO(record): 운동 기록 시안 확정 후 구현. 현재는 진입/뒤로가기만 동작하는 플레이스홀더.
@Composable
fun RecordHealthScreen(viewModel: RecordHealthViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        RecordTopBar(
            title = "운동 기록",
            onBackClick = { viewModel.onIntent(RecordHealthIntent.BackClicked) },
        )
    }
}
