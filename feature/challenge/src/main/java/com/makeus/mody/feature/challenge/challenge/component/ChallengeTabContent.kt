package com.makeus.mody.feature.challenge.challenge.component

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyLoadingScreen
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** 달성 걸음수 강조 컬러(시안 지정 — 팔레트 외 값). */
private val StepYellow = Color(0xFFF3D42F)

/**
 * 챌린지 탭: 그룹 필수(걸음 수 게이지 + 기여도 순위) + 그룹 선택(주간 챌린지 목록).
 */
@Composable
fun ChallengeTabContent(
    isLoading: Boolean,
    stepChallenge: StepChallengeStatus?,
    stepRankings: List<StepRanking>,
    weeklyChallenges: List<WeeklyChallenge>,
    onStepRefreshClick: () -> Unit,
    onChangeStepChallengeClick: () -> Unit,
    onWeeklyChallengeClick: (Long) -> Unit,
) {
    if (isLoading) {
        ModyLoadingScreen()
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        StepChallengeSection(
            step = stepChallenge,
            onRefreshClick = onStepRefreshClick,
            onChangeClick = onChangeStepChallengeClick,
        )
        RankingSection(rankings = stepRankings)
        WeeklySection(
            challenges = weeklyChallenges,
            onChallengeClick = onWeeklyChallengeClick,
        )
    }
}

/** 그룹 필수 챌린지: 칩/제목 + 반원 게이지 + 달성/목표 걸음수. */
@Composable
private fun StepChallengeSection(
    step: StepChallengeStatus?,
    onRefreshClick: () -> Unit,
    onChangeClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                SectionChip(text = "그룹 필수 챌린지")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = step?.title ?: "진행 중인 챌린지가 없어요",
                    style = ModyTheme.typography.h3,
                    color = ModyTheme.colors.gray10,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (step != null) "모디그룹, 목표까지 얼마 안 남았어요!" else "챌린지를 선택해보세요!",
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray07,
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ModyTheme.colors.gray09)
                    .clickable(onClick = onChangeClick)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "챌린지 변경",
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.white,
                )
            }
        }

        if (step != null) {
            Spacer(modifier = Modifier.height(20.dp))
            StepGauge(percent = step.progressPercent)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "달성 걸음수",
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray08,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            ModyTheme.typography.b5.toSpanStyle().copy(color = StepYellow),
                        ) { append(formatSteps(step.currentStepCount)) }
                        withStyle(
                            ModyTheme.typography.c1.toSpanStyle().copy(color = StepYellow),
                        ) { append("보") }
                    },
                    style = ModyTheme.typography.b5,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(ModyIcons.Reset),
                    contentDescription = "걸음 수 새로고침",
                    tint = ModyTheme.colors.gray04,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onRefreshClick),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "목표 걸음수",
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray08,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        withStyle(ModyTheme.typography.c1.toSpanStyle()) {
                            append(formatSteps(step.targetStepCount))
                        }
                        withStyle(ModyTheme.typography.c3.toSpanStyle()) { append("보") }
                    },
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray10,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
    }
}

/** 반원 게이지 + 중앙 %/걷는 캐릭터 + 0/100 라벨. */
@Composable
private fun StepGauge(percent: Int) {
    val trackColor = ModyTheme.colors.gray01
    val progressColor = ModyTheme.colors.primary100
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(150.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 24.dp.toPx()
                val diameter = minOf(size.width, size.height * 2f) - stroke
                val topLeft = Offset((size.width - diameter) / 2f, size.height * 2f - diameter - stroke / 2f)
                val arcSize = Size(diameter, diameter)
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = progressColor,
                    startAngle = 180f,
                    sweepAngle = 180f * percent / 100f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(ModyTheme.typography.h1.toSpanStyle()) { append("$percent") }
                        withStyle(
                            ModyTheme.typography.b1.toSpanStyle().copy(fontSize = 22.sp),
                        ) { append("%") }
                    },
                    style = ModyTheme.typography.h1,
                    color = ModyTheme.colors.gray10,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(R.drawable.img_challenge_walker),
                    contentDescription = null,
                    modifier = Modifier.size(width = 65.dp, height = 82.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "0", style = ModyTheme.typography.c2, color = ModyTheme.colors.gray06)
            Text(text = "100", style = ModyTheme.typography.c2, color = ModyTheme.colors.gray06)
        }
    }
}

/** 현재 기여도 순위: 1~3등 포디움 + 4등 이하 리스트. */
@Composable
private fun RankingSection(rankings: List<StepRanking>) {
    if (rankings.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "현재 기여도 순위",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )

        val top3 = rankings.filter { it.rank in 1..3 }.sortedBy { it.rank }
        val rest = rankings.filter { it.rank > 3 }.sortedBy { it.rank }

        if (top3.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(52.dp, Alignment.CenterHorizontally),
            ) {
                top3.forEach { TopRankColumn(it) }
            }
        }
        if (rest.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            rest.forEachIndexed { index, ranking ->
                if (index > 0) Spacer(modifier = Modifier.height(20.dp))
                RankRow(ranking)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TopRankColumn(ranking: StepRanking) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (ranking.rank == 1) {
                Icon(
                    painter = painterResource(ModyIcons.Crown),
                    contentDescription = null,
                    tint = ModyTheme.colors.primary100,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "${ranking.rank}등",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ModyAvatar(
            imageUrl = ranking.profileImageUrl,
            size = 44.dp,
            modifier = Modifier.border(
                width = if (ranking.rank == 1) 1.4.dp else 1.2.dp,
                color = if (ranking.rank == 1) ModyTheme.colors.primary100 else ModyTheme.colors.gray02,
                shape = CircleShape,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ranking.nickname,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(4.dp))
        StepCountText(ranking.stepCount)
    }
}

@Composable
private fun RankRow(ranking: StepRanking) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${ranking.rank}등",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.width(12.dp))
        ModyAvatar(imageUrl = ranking.profileImageUrl, size = 36.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = ranking.nickname,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
            modifier = Modifier.weight(1f),
        )
        StepCountText(ranking.stepCount)
    }
}

/** "N,NNN보" — 숫자 16 SemiBold + 단위 14 Medium, gray07. */
@Composable
private fun StepCountText(stepCount: Int) {
    Text(
        text = buildAnnotatedString {
            withStyle(ModyTheme.typography.b6.toSpanStyle()) { append(formatSteps(stepCount)) }
            withStyle(ModyTheme.typography.c2.toSpanStyle()) { append("보") }
        },
        style = ModyTheme.typography.b6,
        color = ModyTheme.colors.gray07,
    )
}

/** 그룹 선택 챌린지: 주간 챌린지 카드 목록. */
@Composable
private fun WeeklySection(
    challenges: List<WeeklyChallenge>,
    onChallengeClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModyTheme.colors.gray01)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 32.dp),
    ) {
        SectionChip(text = "그룹 선택 챌린지")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "이번주 주간 챌린지",
            style = ModyTheme.typography.h3,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "원하는 챌린지에 참여해 인증해보세요!",
            style = ModyTheme.typography.c2,
            color = ModyTheme.colors.gray07,
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (challenges.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.white),
            ) {
                challenges.forEach { challenge ->
                    WeeklyChallengeRow(
                        challenge = challenge,
                        onClick = { onChallengeClick(challenge.groupChallengeId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeRow(
    challenge: WeeklyChallenge,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ModyTheme.colors.gray09)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = dDayLabel(challenge.deadlineDayOfWeek),
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.white,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = challenge.title,
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(ModyIcons.Right),
                contentDescription = null,
                tint = ModyTheme.colors.gray05,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = participantLabel(challenge),
            style = ModyTheme.typography.c2,
            color = ModyTheme.colors.gray08,
        )
    }
}

/** "버디1님 외 4명이 참여하고 있어요!" — 참여자 없으면 참여 유도 문구. */
private fun participantLabel(challenge: WeeklyChallenge): String {
    val nickname = challenge.randomParticipantNickname
    return when {
        challenge.participantCount <= 0 || nickname.isNullOrBlank() ->
            "아직 참여한 버디가 없어요. 먼저 참여해보세요!"
        challenge.participantCount == 1 -> "${nickname}님이 참여하고 있어요!"
        else -> "${nickname}님 외 ${challenge.participantCount - 1}명이 참여하고 있어요!"
    }
}

/** 마감 요일(서버 enum MONDAY~SUNDAY) → 오늘 기준 D-N. 파싱 실패 시 빈 문자열 대신 요일 그대로. */
private fun dDayLabel(deadlineDayOfWeek: String): String {
    val target = runCatching {
        DayOfWeek.valueOf(deadlineDayOfWeek.uppercase(Locale.US))
    }.getOrNull() ?: return deadlineDayOfWeek
    val today = LocalDate.now().dayOfWeek
    val days = (target.value - today.value + 7) % 7
    return "D-$days"
}

private fun formatSteps(steps: Int): String = "%,d".format(steps)

@Composable
private fun SectionChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(ModyTheme.colors.primary300)
            .border(1.dp, ModyTheme.colors.primary0, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray08,
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ChallengeTabContentPreview() {
    ModyTheme {
        ChallengeTabContent(
            isLoading = false,
            stepChallenge = StepChallengeStatus(
                groupChallengeId = 1,
                title = "수원까지 걸어가기 챌린지",
                targetStepCount = 45000,
                currentStepCount = 38000,
            ),
            stepRankings = listOf(
                StepRanking(1, 1, "나는야화영", null, 15000),
                StepRanking(2, 2, "예은", null, 14000),
                StepRanking(3, 3, "동준", null, 13000),
                StepRanking(4, 4, "도윤", null, 10000),
                StepRanking(5, 5, "민석", null, 4000),
            ),
            weeklyChallenges = listOf(
                WeeklyChallenge(1, "이번주의 고해성사하기", "SUNDAY", 5, "버디1"),
                WeeklyChallenge(2, "하루에 줄넘기 15분 하기", "FRIDAY", 5, "버디1"),
            ),
            onStepRefreshClick = {},
            onChangeStepChallengeClick = {},
            onWeeklyChallengeClick = {},
        )
    }
}
