package com.makeus.mody.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.LocalBottomBarInset
import com.makeus.mody.core.designsystem.theme.ModyTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.makeus.mody.feature.challenge.challenge.ChallengeScreen
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

    // 탭 전환 + 다른 화면에 갔다 돌아온 경우(NavHost 가 이 컴포저블을 버렸다 다시 만든다)
    // 모두 화면 노출로 남긴다. ViewModel init 은 재진입 때 다시 돌지 않아 여기서 부른다.
    LaunchedEffect(selectedTab) { viewModel.trackCurrentTabScreen() }

    // 하단 탭 바는 콘텐츠 위에 떠서 뒤를 흐리게 비춘다(시안 menu 35:3564, 글래스).
    // 콘텐츠는 화면 맨 아래까지 깔고, 탭 화면이 스크롤 끝에 바 높이만큼 여백을 두도록 알려 준다.
    val hazeState = remember { HazeState() }
    val bottomBarInset = MainBottomBarContentHeight +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ModyTheme.colors.white)
                // 바가 흐리게 비출 원본. 바 자신은 이 밖에 있어야 자기 자신을 비추지 않는다.
                .haze(hazeState)
                .statusBarsPadding(),
        ) {
            CompositionLocalProvider(LocalBottomBarInset provides bottomBarInset) {
                when (selectedTab) {
                    MainTab.FEED -> FeedScreen(viewModel = feedViewModel)
                    MainTab.CHALLENGE -> ChallengeScreen()
                    MainTab.MY -> MyPageScreen()
                }
            }
        }
        MainBottomBar(
            tabs = visibleTabs,
            selected = selectedTab,
            onSelect = viewModel::selectTab,
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

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

