package com.makeus.mody.feature.challenge.stepchange

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Canvas
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.core.designsystem.component.ModyTextSkeleton
import com.makeus.mody.core.designsystem.modifier.shimmer
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.StepChallengeOption
import com.makeus.mody.feature.challenge.stepchange.contract.StepChallengeChangeIntent
import com.makeus.mody.feature.challenge.stepchange.contract.StepChallengeChangeState

/** 그룹 필수(걸음 수) 챌린지 변경 — 목록에서 고르면 확인 후 교체한다. */
@Composable
fun StepChallengeChangeScreen(
    viewModel: StepChallengeChangeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onIntent(StepChallengeChangeIntent.ScreenEntered) }

    StepChallengeChangeContent(state = state, onIntent = viewModel::onIntent)

    // 교체하면 서버에서 기존 걸음 기록이 초기화되므로 한 번 되묻는다.
    state.pendingOption?.let {
        ModyDialog(
            title = "정말 챌린지를 변경하시겠어요?",
            message = "지금까지 걸었던 기록이 전부 사라져요!",
            confirmText = "변경하기",
            dismissText = "취소",
            confirmEnabled = !state.isChanging,
            onConfirm = { viewModel.onIntent(StepChallengeChangeIntent.ChangeConfirmed) },
            onDismissRequest = { viewModel.onIntent(StepChallengeChangeIntent.ChangeCancelled) },
        )
    }

    ModyErrorDialog(
        message = state.error,
        onDismiss = { viewModel.onIntent(StepChallengeChangeIntent.ErrorShown) },
    )
}

@Composable
private fun StepChallengeChangeContent(
    state: StepChallengeChangeState,
    onIntent: (StepChallengeChangeIntent) -> Unit,
) {
    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "챌린지 변경",
                onBackClick = { onIntent(StepChallengeChangeIntent.BackClicked) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // 안내 문구는 데이터가 아니라 로딩 중에도 그대로 둔다 — 화면 골격이 먼저 서야
            // 목록이 도착할 때 아래로 밀리지 않는다.
            Text(
                text = "다른 챌린지를 선택해주세요.",
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (state.isLoading) {
                StepChallengeOptionSkeletonList()
            } else {
                state.options.forEachIndexed { index, option ->
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    StepChallengeOptionCard(
                        option = option,
                        onClick = {
                            onIntent(StepChallengeChangeIntent.OptionClicked(option.challengeId))
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 목록 자리 스켈레톤. 개수를 모르므로 고정 [count]장.
 *
 * [StepChallengeOptionCard] 와 같은 파일에 두는 건 레이아웃을 그대로 베껴야 하기 때문이다 —
 * 카드 쪽 패딩·간격이 바뀌면 여기도 같이 고쳐야 값이 도착할 때 화면이 안 튄다.
 */
@Composable
private fun StepChallengeOptionSkeletonList(count: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.gray01)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 배지와 같은 60dp — 카드 높이를 이 원이 정하므로 크기가 어긋나면 안 된다.
                Box(modifier = Modifier.size(60.dp).shimmer(CircleShape))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModyTextSkeleton(
                        width = 168.dp,
                        lineHeight = ModyTheme.typography.b6.lineHeight,
                    )
                    ModyTextSkeleton(
                        width = 104.dp,
                        lineHeight = ModyTheme.typography.c2.lineHeight,
                    )
                }
            }
        }
    }
}

/**
 * 선택지 카드. 시안의 세 상태를 그린다.
 *
 * - 진행 중([StepChallengeOption.selected]): 연한 노랑 배경 + 노랑 테두리. 배지는 없고 색으로만
 *   구분한다. 바꿀 게 없으므로 탭도 받지 않는다 — 색이 그 이유를 대신 설명한다.
 * - 완료([StepChallengeOption.completed]): 우측에 "완료" 배지. 배경은 일반과 같다.
 * - 일반: gray01 배경.
 */
@Composable
private fun StepChallengeOptionCard(
    option: StepChallengeOption,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (option.selected) ModyTheme.colors.primary400 else ModyTheme.colors.gray01,
            )
            .then(
                if (option.selected) {
                    Modifier.border(1.5.dp, ModyTheme.colors.primary100, shape)
                } else {
                    Modifier
                },
            )
            // 이미 진행 중인 챌린지는 바꿀 게 없어 탭을 받지 않는다.
            .clickable(enabled = !option.selected, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepChallengeBadge(targetStepCount = option.targetStepCount)
        // weight(1f): 제목이 길어도 "완료" 배지를 밀어내지 않고 두 줄로 접힌다.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = option.title,
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
            Text(
                text = "${formatDistance(option.distanceKm)}km / ${formatSteps(option.targetStepCount)}",
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.gray08,
            )
        }
        // 진행 중인 카드에는 붙이지 않는다 — 시안상 그 자리는 비어 있고, 두 상태가 겹치면
        // "완료했는데 또 진행 중"으로 읽힌다.
        if (option.completed && !option.selected) {
            CompletedBadge()
        }
    }
}

/** "완료" 배지 — 이미 달성한 챌린지. */
@Composable
private fun CompletedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(ModyTheme.colors.primary100)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "완료",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray10,
            maxLines = 1,
        )
    }
}

/** 60dp 원형 배지 — 점선 링 + 신발 일러스트 + 목표 걸음수. 시안 좌표 그대로 배치. */
@Composable
private fun StepChallengeBadge(targetStepCount: Int) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(ModyTheme.colors.gray09),
    ) {
        val ringColor = ModyTheme.colors.primary100
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            // 시안 실측: 선 1dp, 점선 1.1dp 그리기 / 2dp 띄우기.
            drawCircle(
                color = ringColor,
                radius = size.minDimension / 2f,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(1.1.dp.toPx(), 2.dp.toPx()),
                    ),
                ),
            )
        }
        Box(
            modifier = Modifier.offset(x = 15.dp, y = 9.dp).size(width = 31.dp, height = 25.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_step_shoe),
                contentDescription = null,
                modifier = Modifier.size(width = 28.dp, height = 22.dp).rotate(8.24f),
            )
        }
        Text(
            text = formatSteps(targetStepCount),
            style = ModyTheme.typography.c3,
            color = ModyTheme.colors.primary100,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 33.dp),
        )
    }
}

/** 150,000 → "15만보". 만 단위로 안 떨어지면 천 단위 구분자를 쓴다. */
private fun formatSteps(steps: Int): String = when {
    steps >= 10_000 && steps % 10_000 == 0 -> "${steps / 10_000}만보"
    else -> "%,d보".format(steps)
}

/** 60.0 → "60", 12.5 → "12.5". */
private fun formatDistance(km: Double): String =
    if (km % 1.0 == 0.0) km.toInt().toString() else "%.1f".format(km)

@Preview(showBackground = true, heightDp = 500, name = "로딩(스켈레톤)")
@Composable
private fun StepChallengeChangeLoadingPreview() {
    ModyTheme {
        StepChallengeChangeContent(
            state = StepChallengeChangeState(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun StepChallengeChangeContentPreview() {
    ModyTheme {
        StepChallengeChangeContent(
            state = StepChallengeChangeState(
                isLoading = false,
                // 시안 그대로 세 상태를 한 화면에서 확인 — 진행 중 / 완료 / 일반.
                options = listOf(
                    StepChallengeOption(
                        1, "서울에서 인천까지 걸어가기", "서울", "인천", 60.0, 150_000,
                        selected = true,
                    ),
                    StepChallengeOption(
                        2, "서울에서 천안까지 걸어가기", "서울", "천안", 90.0, 200_000,
                        selected = false, completed = true,
                    ),
                    StepChallengeOption(
                        3, "서울에서 대전까지 걸어가기", "서울", "대전", 160.0, 300_000,
                        selected = false,
                    ),
                    StepChallengeOption(
                        4, "서울에서 부산까지 걸어가기", "서울", "부산", 325.0, 500_000,
                        selected = false,
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
