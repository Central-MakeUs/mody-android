package com.makeus.mody.feature.challenge.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Text
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyLogoTopBar
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.challenge.challenge.component.ChallengeTabContent
import com.makeus.mody.feature.challenge.challenge.component.StreakTabContent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeIntent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeState
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeSubTab

/** 실제 진입점: ViewModel 상태를 stateless [ChallengeContent] 로 흘려보낸다. */
@Composable
fun ChallengeScreen(viewModel: ChallengeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // 탭 진입/복귀 시 재조회(기록 작성 후 돌아오면 버디 상태 반영). VM은 유지되므로 재조회 필요.
    LaunchedEffect(Unit) { viewModel.onIntent(ChallengeIntent.ScreenEntered) }
    ChallengeContent(state = state, onIntent = viewModel::onIntent)

    ModyErrorDialog(
        message = state.error,
        onDismiss = { viewModel.onIntent(ChallengeIntent.ErrorShown) },
    )
}

/** 상태를 받아 렌더만 하는 stateless 화면. 프리뷰/테스트는 여기에 직접 상태를 넣는다. */
@Composable
private fun ChallengeContent(
    state: ChallengeState,
    onIntent: (ChallengeIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white),
    ) {
        ModyLogoTopBar(onAlarmClick = { onIntent(ChallengeIntent.AlarmClicked) })

        SubTabRow(
            selected = state.selectedSubTab,
            onSelect = { onIntent(ChallengeIntent.SubTabSelected(it)) },
        )

        // weight(1f): 탭 콘텐츠는 남은 높이만 차지 — fillMaxSize 콘텐츠가 하단 바 뒤로 밀리지 않게.
        Box(modifier = Modifier.weight(1f)) {
            when (state.selectedSubTab) {
                ChallengeSubTab.STREAK -> StreakTabContent(
                    isLoading = state.isLoading,
                    summary = state.summary,
                    buddies = state.buddies,
                    nudgingMemberIds = state.nudgingMemberIds,
                    onNudgeClick = { onIntent(ChallengeIntent.NudgeClicked(it)) },
                )
                ChallengeSubTab.CHALLENGE -> ChallengeTabContent(
                    isLoading = state.isLoading,
                    stepChallenge = state.stepChallenge,
                    stepRankings = state.stepRankings,
                    weeklyChallenges = state.weeklyChallenges,
                    onStepRefreshClick = { onIntent(ChallengeIntent.StepRefreshClicked) },
                    onChangeStepChallengeClick = { onIntent(ChallengeIntent.ChangeStepChallengeClicked) },
                    onWeeklyChallengeClick = { onIntent(ChallengeIntent.WeeklyChallengeClicked(it)) },
                )
            }
        }
    }
}

/** 상단 서브탭(연속 기록/챌린지). 선택 탭은 gray10 + 하단 인디케이터, 미선택은 gray04. */
@Composable
private fun SubTabRow(
    selected: ChallengeSubTab,
    onSelect: (ChallengeSubTab) -> Unit,
) {
    Column {
        // selectableGroup + Role.Tab: 접근성 서비스에 상호배타 탭 그룹으로 노출.
        Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            ChallengeSubTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                        ) { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.height(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.label,
                            style = ModyTheme.typography.b3,
                            color = if (isSelected) ModyTheme.colors.gray10 else ModyTheme.colors.gray04,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (isSelected) ModyTheme.colors.gray10 else ModyTheme.colors.white,
                            ),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray01),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeContentPreview() {
    ModyTheme {
        ChallengeContent(state = ChallengeState(), onIntent = {})
    }
}
