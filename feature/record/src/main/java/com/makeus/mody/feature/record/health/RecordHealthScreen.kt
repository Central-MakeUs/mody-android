package com.makeus.mody.feature.record.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.component.ModyBackButton
import com.makeus.mody.core.designsystem.theme.ModyTheme
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModyBackButton(onClick = { viewModel.onIntent(RecordHealthIntent.BackClicked) })
            Text(
                text = "운동 기록",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
        }
    }
}
