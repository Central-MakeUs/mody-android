package com.makeus.mody.feature.feed.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.makeus.mody.core.navigation.FeedGraph
import com.makeus.mody.feature.feed.detail.RecordDetailScreen

/**
 * 피드 관련 풀스크린 route. 피드 메인은 바텀탭(MainScreen) 내부에 있고,
 * 여기엔 탭 위로 push 되는 기록 상세를 등록한다.
 */
fun NavGraphBuilder.feedNavGraph() {
    composable<FeedGraph.RecordDetailRoute> { RecordDetailScreen() }
}
