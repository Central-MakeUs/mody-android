package com.makeus.mody.feature.challenge.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.makeus.mody.core.navigation.ChallengeGraph
import com.makeus.mody.feature.challenge.stepchange.StepChallengeChangeScreen

/**
 * 챌린지 관련 풀스크린 route. 챌린지 메인은 바텀탭(MainScreen) 내부에 있고,
 * 여기엔 탭 위로 push 되는 챌린지 변경 화면을 등록한다.
 */
fun NavGraphBuilder.challengeNavGraph() {
    composable<ChallengeGraph.StepChallengeChangeRoute> { StepChallengeChangeScreen() }
}
