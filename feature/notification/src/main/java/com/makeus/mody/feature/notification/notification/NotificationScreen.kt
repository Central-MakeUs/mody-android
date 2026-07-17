package com.makeus.mody.feature.notification.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.notification.notification.contract.NotificationIntent

// TODO(notification): 알림 목록 시안 확정 후 구현. 현재는 진입/뒤로가기만 동작하는 스켈레톤.
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ModyBackTopBar(
            title = "알림",
            onBackClick = { viewModel.onIntent(NotificationIntent.BackClicked) },
        )
    }
}
