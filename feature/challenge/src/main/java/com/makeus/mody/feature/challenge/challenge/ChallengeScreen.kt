package com.makeus.mody.feature.challenge.challenge

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Text
import com.makeus.mody.core.commonui.health.openHealthConnect
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyLogoTopBar
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.challenge.challenge.component.ChallengeTabContent
import com.makeus.mody.feature.challenge.challenge.component.SoloGroupEmpty
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

    // Health Connect 권한 요청. 허용 집합을 그대로 돌려주므로 요청한 권한이 전부 포함됐는지로 판정.
    // 요청 집합은 화면 로컬에 따로 붙든다 — 런처 콜백은 항상 최신 값을 읽으므로,
    // 런처를 띄운 직후 비우는 state 를 그대로 참조하면 결과가 늘 '거부'로 판정된다.
    var pendingHealthPermissions by remember { mutableStateOf<Set<String>?>(null) }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions ->
        val requested = pendingHealthPermissions
        pendingHealthPermissions = null
        viewModel.onIntent(
            ChallengeIntent.HealthPermissionResult(
                granted = requested != null && grantedPermissions.containsAll(requested),
            ),
        )
    }
    LaunchedEffect(state.healthPermissionRequest) {
        val permissions = state.healthPermissionRequest
        if (permissions.isNullOrEmpty()) return@LaunchedEffect
        pendingHealthPermissions = permissions
        healthPermissionLauncher.launch(permissions)
        viewModel.onIntent(ChallengeIntent.HealthPermissionRequestLaunched)
    }

    // 콕 찌르기 성공 안내. 다른 화면(알림 설정 등)과 같은 Toast 방식.
    val context = LocalContext.current
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(ChallengeIntent.ToastShown)
        }
    }

    // 안내에서 "설정 열기" → Health Connect(시스템). 마이페이지 연동 설정과 같은 진입 경로.
    LaunchedEffect(state.healthSettingsRequest) {
        state.healthSettingsRequest?.let {
            openHealthConnect(context, it)
            viewModel.onIntent(ChallengeIntent.HealthSettingsLaunched)
        }
    }

    ChallengeContent(state = state, onIntent = viewModel::onIntent)

    // 권한 거부 안내. 두 번 거부한 뒤로는 시스템 요청이 다이얼로그 없이 즉시 거부로 돌아와
    // 새로고침이 먹통처럼 보이므로, 남은 경로(Health Connect 설정)를 알려준다.
    if (state.showHealthPermissionGuide) {
        ModyDialog(
            title = "걸음 수를 가져올 수 없어요",
            message = "건강 데이터 접근을 허용해야 걸음 수가 챌린지에 반영돼요.\n" +
                "설정에서 모디의 걸음 수 읽기 권한을 켜주세요.",
            confirmText = "설정 열기",
            onConfirm = { viewModel.onIntent(ChallengeIntent.HealthSettingsClicked) },
            dismissText = "나중에",
            onDismissRequest = {
                viewModel.onIntent(ChallengeIntent.HealthPermissionGuideDismissed)
            },
        )
    }

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
        ModyLogoTopBar(
            onAlarmClick = { onIntent(ChallengeIntent.AlarmClicked) },
            hasUnreadNotification = state.hasUnreadNotification,
        )

        SubTabRow(
            selected = state.selectedSubTab,
            onSelect = { onIntent(ChallengeIntent.SubTabSelected(it)) },
        )

        // weight(1f): 탭 콘텐츠는 남은 높이만 차지 — fillMaxSize 콘텐츠가 하단 바 뒤로 밀리지 않게.
        Box(modifier = Modifier.weight(1f)) {
            // 나 혼자인 그룹이면 두 탭 다 성립하지 않는다. 로딩 중엔 아직 판정 전이라 넘긴다.
            if (!state.isLoading && state.isSoloGroup) {
                SoloGroupEmpty(tab = state.selectedSubTab)
                return@Box
            }
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
                    weeklyLoaded = state.weeklyLoaded,
                    onStepRefreshClick = { onIntent(ChallengeIntent.StepRefreshClicked) },
                    onChangeStepChallengeClick = { onIntent(ChallengeIntent.ChangeStepChallengeClicked) },
                    onWeeklyChallengeClick = { onIntent(ChallengeIntent.WeeklyChallengeClicked(it)) },
                )
            }
        }
    }
}

/**
 * 상단 서브탭(연속 기록/챌린지).
 * 하단 인디케이터가 선택 탭 gray10 / 미선택 gray02 로 전체 폭을 채워 구분선 역할까지 겸한다.
 */
@Composable
private fun SubTabRow(
    selected: ChallengeSubTab,
    onSelect: (ChallengeSubTab) -> Unit,
) {
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
                    // 시안 실측: 탭 바 전체 52 = 텍스트 영역 50 + 인디케이터 2.
                    modifier = Modifier.height(50.dp),
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
                            if (isSelected) ModyTheme.colors.gray10 else ModyTheme.colors.gray02,
                        ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeContentPreview() {
    ModyTheme {
        ChallengeContent(state = ChallengeState(), onIntent = {})
    }
}
