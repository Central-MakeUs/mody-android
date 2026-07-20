package com.makeus.mody.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.feed.FeedScreen
import com.makeus.mody.feature.feed.feed.FeedViewModel
import com.makeus.mody.feature.feed.feed.component.FeedWriteFab
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.mypage.MyPageScreen

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val visibleTabs by viewModel.visibleTabs.collectAsState()
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedState by feedViewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ModyTheme.colors.white)
                .statusBarsPadding(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    MainTab.FEED -> FeedScreen(viewModel = feedViewModel)
                    MainTab.CHALLENGE -> ChallengeTabPlaceholder()
                    MainTab.MY -> MyPageScreen()
                }
            }
            MainBottomBar(
                tabs = visibleTabs,
                selected = selectedTab,
                onSelect = viewModel::selectTab,
            )
        }

        // 피드 FAB 딤 오버레이: statusBarsPadding 밖(이 Box 레벨)에서 그려야
        // 하단 네비게이션 바와 상태바 영역까지 함께 어두워진다.
        if (selectedTab == MainTab.FEED) {
            FeedWriteFab(
                expanded = feedState.isFabExpanded,
                onFabClick = { feedViewModel.onIntent(FeedIntent.FabClicked) },
                onDismiss = { feedViewModel.onIntent(FeedIntent.FabDismissed) },
                onWriteExercise = { feedViewModel.onIntent(FeedIntent.WriteExerciseClicked) },
                onWriteMeal = { feedViewModel.onIntent(FeedIntent.WriteMealClicked) },
                // 하단 네비 바(콘텐츠 높이)만큼 더 띄워 FAB이 바 위에 오도록.
                fabBottomPadding = 12.dp + MainBottomBarContentHeight,
            )
        }
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

