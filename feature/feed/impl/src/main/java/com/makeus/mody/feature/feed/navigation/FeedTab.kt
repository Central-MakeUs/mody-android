package com.makeus.mody.feature.feed.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.feature.feed.feed.FeedScreen
import com.makeus.mody.feature.feed.feed.FeedViewModel
import com.makeus.mody.feature.feed.feed.component.FeedWriteFab
import com.makeus.mody.feature.feed.feed.contract.FeedIntent

/**
 * 하단 탭 "피드" 본문. 앱 셸(MainScreen)이 탭 영역에 그린다.
 *
 * [FeedTabWriteFab] 과 같은 ViewModelStoreOwner(Main 백스택 엔트리) 안에서 불리므로
 * 둘은 같은 FeedViewModel 을 받는다.
 */
@Composable
fun FeedTab() {
    FeedScreen()
}

/**
 * 피드 탭의 기록 작성 FAB + 딤 오버레이.
 *
 * 하단 네비게이션 바·상태바까지 덮어야 해서 탭 본문과 따로, 셸의 최상위 Box 에서 그린다.
 * @param fabBottomPadding 셸의 하단 바 높이를 더한 FAB 하단 여백.
 */
@Composable
fun FeedTabWriteFab(fabBottomPadding: Dp) {
    val viewModel: FeedViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeedWriteFab(
        expanded = state.isFabExpanded,
        onFabClick = { viewModel.onIntent(FeedIntent.FabClicked) },
        onDismiss = { viewModel.onIntent(FeedIntent.FabDismissed) },
        onWriteExercise = { viewModel.onIntent(FeedIntent.WriteExerciseClicked) },
        onWriteMeal = { viewModel.onIntent(FeedIntent.WriteMealClicked) },
        fabBottomPadding = fabBottomPadding,
    )
}
