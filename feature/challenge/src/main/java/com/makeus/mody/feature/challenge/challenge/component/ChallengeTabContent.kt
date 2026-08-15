package com.makeus.mody.feature.challenge.challenge.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.makeus.mody.core.domain.model.WeeklyChallengeParticipant
import com.makeus.mody.feature.challenge.util.dDayLabel

/** 달성 걸음수 강조 컬러(시안 지정 — 팔레트 외 값). */
private val StepYellow = Color(0xFFF3D42F)

/** 주간 챌린지 카드에 얼굴을 보여줄 참여자 수. 넘치는 인원은 "+N". */
private const val ParticipantAvatarMax = 3

/** 시안 실측: 아바타 24, 겹침 6 (가로 피치 18). */
private val ParticipantAvatarSize = 24.dp
private val ParticipantAvatarOverlap = 6.dp

/** 1등 왕관 아이콘 크기(시안 251:2571). 반대쪽 여백도 같은 값을 쓴다. */
private val CrownSize = 20.dp

/** 게이지 좌우 여백 — 섹션 패딩(24) 안쪽으로 더 들어가는 값. 시안 left 54 기준. */
private val GaugeInset = 30.dp

/** 호 윗변 ~ %/캐릭터 묶음 윗변 간격. 시안: 호 322 → % 358. */
private val GaugeContentTop = 36.dp

/** 캐릭터 크기(시안 64.63 x 81.87). */
private val WalkerWidth = 65.dp
private val WalkerHeight = 82.dp

/**
 * 게이지 영역 높이 = 위 여백 + %(28sp 줄높이 39.2) + 캐릭터.
 * 반원 높이(146.5)보다 커서 캐릭터가 호 밑변 아래로 내려오고, 그만큼 아래 요소가 밀린다.
 */
private val GaugeHeight = GaugeContentTop + 40.dp + WalkerHeight

/** 호 밑변 ~ 0/100 라벨 윗변 간격. */
private val GaugeLabelGap = 6.dp

/** "100" 은 호 오른쪽 끝(347)보다 더 나간다(시안 우측 끝 353). */
private val GaugeMaxLabelOffset = 6.dp

/**
 * 챌린지 탭: 그룹 필수(걸음 수 게이지 + 기여도 순위) + 그룹 선택(주간 챌린지 목록).
 */
@Composable
fun ChallengeTabContent(
    isLoading: Boolean,
    stepChallenge: StepChallengeStatus?,
    stepRankings: List<StepRanking>,
    weeklyChallenges: List<WeeklyChallenge>,
    weeklyLoaded: Boolean,
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
            loaded = weeklyLoaded,
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
    // 시안 실측: 트랙 #E4E4E4(gray02), 진행 #FFE24A(primary100), 선 두께 12.
    val trackColor = ModyTheme.colors.gray02
    val progressColor = ModyTheme.colors.primary100
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // 시안(402dp 프레임): 원 지름 292.95, 좌 54 / 우 55 → 섹션 패딩 24 안쪽으로 30 더 들어간다.
        // 높이는 반원(146.5)이 아니라 캐릭터 밑변까지 잡는다 — 그래야 아래 요소와 겹치지 않는다.
        // 호는 아래 Canvas 가 항상 Box 윗변에 붙여 그리므로 높이를 키워도 위치가 안 밀린다.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GaugeInset)
                .height(GaugeHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 12.dp.toPx()
                // 바깥 지름 = 폭. 중심선 원을 stroke/2 만큼 안쪽으로 넣어 윗변을 Box 상단에 맞춘다.
                val diameter = size.width - stroke
                val topLeft = Offset(stroke / 2f, stroke / 2f)
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
            // %+캐릭터 묶음은 호 윗변에서 36 아래에서 시작한다(시안: 호 322 → % 358).
            // 둘 사이 간격은 0 — %(28 Bold) 줄 높이 39.2 에 캐릭터가 바로 붙고,
            // 그 결과 캐릭터 밑변이 호 밑변보다 약 10 내려온다.
            Column(
                modifier = Modifier.offset(y = GaugeContentTop),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(ModyTheme.typography.h1.toSpanStyle()) { append("$percent") }
                        withStyle(ModyTheme.typography.b1.toSpanStyle()) { append("%") }
                    },
                    style = ModyTheme.typography.h1,
                    color = ModyTheme.colors.gray10,
                )
                Image(
                    painter = painterResource(R.drawable.img_challenge_walker),
                    contentDescription = null,
                    modifier = Modifier.size(width = WalkerWidth, height = WalkerHeight),
                )
            }
            // 0/100 은 호 밑변에 붙는다. 바깥 지름 = Box 폭이므로 호 밑변 y = 폭/2.
            // Box 밖에 두면 캐릭터 높이(158)만큼 아래로 밀려 호와 멀어진다.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = maxWidth / 2 + GaugeLabelGap),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "0", style = ModyTheme.typography.c2, color = ModyTheme.colors.gray06)
                Text(
                    text = "100",
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray06,
                    modifier = Modifier.offset(x = GaugeMaxLabelOffset),
                )
            }
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
            // 폭을 인원수대로 등분한다. 3명이면 3등분, 2명이면 2등분, 1명이면 전체 폭에
            // 가운데 정렬 — 그룹 인원이 적어도 자연스럽게 놓인다.
            //
            // 간격을 고정하면(예전: spacedBy(52)) 열 폭을 내용이 정하게 돼, 닉네임이나
            // 걸음 수 자릿수가 다르면 아바타 사이 거리가 제각각이 되고 2등이 화면
            // 중앙에서 벗어난다. 1등만 왕관이 붙는 것도 열 폭을 키운다.
            //
            // 시안(251:2567)도 간격 52 에 열 폭은 내용이 정하는 구조라 목업 데이터에서만
            // 성립한다. 등분하면 아바타 중심이 83·201·319 로 시안(83·203·321) 대비 최대
            // 2 차이다 — 시안 자체 간격도 120/118 로 균등하지 않다.
            Row(modifier = Modifier.fillMaxWidth()) {
                top3.forEach { TopRankColumn(ranking = it, modifier = Modifier.weight(1f)) }
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
private fun TopRankColumn(ranking: StepRanking, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // 1등만 왕관이 붙는다. 아이콘을 왼쪽에만 두면 [왕관 + 글자] 묶음이 가운데 정렬돼
        // "1등" 글자가 아이콘 폭의 절반(10)만큼 오른쪽으로 밀리고, 2·3등 글자와 좌우가
        // 어긋나 보인다. 시안(251:2570)도 이 상태다 — 1등 글자 중심 45 vs 아바타 중심 35.
        // 반대쪽에 같은 폭을 비워 글자 자체가 열 중앙에 오게 한다.
        Row(verticalAlignment = Alignment.CenterVertically) {
            val hasCrown = ranking.rank == 1
            if (hasCrown) {
                Icon(
                    painter = painterResource(ModyIcons.Crown),
                    contentDescription = null,
                    tint = ModyTheme.colors.primary100,
                    modifier = Modifier.size(CrownSize),
                )
            }
            Text(
                text = "${ranking.rank}등",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
            if (hasCrown) Spacer(modifier = Modifier.width(CrownSize))
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
            // 이름이 길면 줄바꿈돼 그 열만 키가 커지고 아래 걸음 수 줄이 어긋난다.
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            // 이름이 길면 남은 폭 안에서 줄바꿈돼 행 높이가 커지고, 등수·아바타·걸음 수의
            // 세로 정렬이 그 행만 어긋난다.
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
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

/**
 * 그룹 선택 챌린지: 주간 챌린지 카드 목록.
 *
 * 목록이 비어 있어도 [loaded] 가 false 면(아직 못 받았거나 조회 실패) 섹션을 통째로 숨긴다.
 * 네트워크 오류를 "이번주 챌린지가 없음"으로 단정하면 안 되기 때문. 기여도 순위 섹션이
 * 빈 목록에 숨는 것과 같은 규칙.
 */
@Composable
private fun WeeklySection(
    challenges: List<WeeklyChallenge>,
    loaded: Boolean,
    onChallengeClick: (Long) -> Unit,
) {
    if (challenges.isEmpty() && !loaded) return
    val isEmpty = challenges.isEmpty()
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
        // 서브타이틀 자리는 그대로 두고 문구만 바꾼다. 참여할 게 없는데 "참여해보세요"를
        // 띄우면 안 되고, 없다는 사실은 이 줄이 알려주면 충분하다(별도 빈 상태 영역 없음).
        Text(
            text = if (isEmpty) {
                "진행중인 주간 챌린지가 없습니다!"
            } else {
                "원하는 챌린지에 참여해 인증해보세요!"
            },
            style = ModyTheme.typography.c2,
            color = ModyTheme.colors.gray07,
        )

        if (!isEmpty) {
            Spacer(modifier = Modifier.height(16.dp))
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (challenge.participants.isNotEmpty()) {
                ParticipantAvatars(challenge = challenge)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = participantLabel(challenge),
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.gray08,
            )
        }
    }
}

/** 참여자 겹침 아바타 — 앞 [ParticipantAvatarMax]명만 그리고 나머지는 "+N". */
@Composable
private fun ParticipantAvatars(challenge: WeeklyChallenge) {
    val shown = challenge.participants.take(ParticipantAvatarMax)
    // 목록이 잘려 와도 총원은 participantCount 가 기준이다.
    val hidden = challenge.participantCount - shown.size

    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            // 음수 간격으로 겹친다. 뒤 항목이 위에 그려져 시안처럼 오른쪽이 앞으로 온다.
            horizontalArrangement = Arrangement.spacedBy(-ParticipantAvatarOverlap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            shown.forEach { participant ->
                ModyAvatar(
                    imageUrl = participant.profileImageUrl,
                    size = ParticipantAvatarSize,
                    contentDescription = participant.nickname,
                )
            }
        }
        if (hidden > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+$hidden",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray10,
            )
        }
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

private val previewParticipants = (1..5).map {
    WeeklyChallengeParticipant(it.toLong(), "버디$it", null)
}

@Preview(showBackground = true, name = "주간 챌린지 없음")
@Composable
private fun WeeklySectionEmptyPreview() {
    ModyTheme {
        WeeklySection(challenges = emptyList(), loaded = true, onChallengeClick = {})
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
                WeeklyChallenge(1, "이번주의 고해성사하기", "SUNDAY", 5, "버디1", previewParticipants),
                WeeklyChallenge(2, "하루에 줄넘기 15분 하기", "FRIDAY", 5, "버디1", previewParticipants),
            ),
            weeklyLoaded = true,
            onStepRefreshClick = {},
            onChangeStepChallengeClick = {},
            onWeeklyChallengeClick = {},
        )
    }
}
