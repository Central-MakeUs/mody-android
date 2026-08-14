package com.makeus.mody.feature.challenge.challenge.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyLoadingScreen
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeButtonStatus
import com.makeus.mody.core.domain.model.NudgeTarget

/**
 * 연속 기록 탭: 전원 연속 기록 헤더(N일째 + 축하 캐릭터) + 그룹 통계 3종 + 버디 신기록(콕 찌르기).
 */
@Composable
fun StreakTabContent(
    isLoading: Boolean,
    summary: ChallengeSummary?,
    buddies: List<NudgeTarget>,
    nudgingMemberIds: Set<Long>,
    onNudgeClick: (Long) -> Unit,
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
        StreakHeader(summary = summary)
        BuddySection(
            buddies = buddies,
            nudgingMemberIds = nudgingMemberIds,
            onNudgeClick = onNudgeClick,
        )
    }
}

/**
 * "N일째 전원 연속 기록 완료!" + 축하 캐릭터 + 통계 3열.
 *
 * 요약이 없으면(조회 실패/로딩 전) 섹션을 통째로 숨긴다. 0 으로 채워 그리면
 * "0일째 전원 연속 기록 완료!" 가 축하 캐릭터와 함께 떠서 데이터가 있는 것처럼 보인다.
 * 기여도 순위·주간 챌린지 섹션과 동일한 규칙.
 */
@Composable
private fun StreakHeader(summary: ChallengeSummary?) {
    if (summary == null) return
    // 이 영역만 좌우 36 이다(아래 버디 섹션은 24). 시안 연속기록 블록·통계 블록 모두 x36,
    // 통계 블록 폭 330 = 402 - 36*2. 이 폭이라야 SpaceBetween 간격이 시안의 22.5 가 된다.
    Column(modifier = Modifier.padding(horizontal = 36.dp)) {
        // 시안 실측 35.5 (탭 바 끝 162 → 캐릭터 윗변 197.5). 텍스트가 아니라 캐릭터가
        // 행의 위아래를 정한다 — 캐릭터 91 이 텍스트 묶음 78.4 보다 크다.
        Spacer(modifier = Modifier.height(36.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                // 두 글자 크기가 달라 Bottom 정렬은 어긋난다. 숫자의 lineHeight(50.4sp)가
                // 글자 아래 여백으로 붙어 글자만 위로 뜨기 때문. 베이스라인으로 맞춘다.
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${summary.allMemberRecordedDays}",
                        style = ModyTheme.typography.h1.copy(fontSize = 36.sp, lineHeight = 50.4.sp),
                        color = ModyTheme.colors.gray09,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = "일째",
                        style = ModyTheme.typography.h1,
                        color = ModyTheme.colors.gray09,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(
                    text = "전원 연속 기록 완료!",
                    style = ModyTheme.typography.b2,
                    color = ModyTheme.colors.gray09,
                )
            }
            Image(
                painter = painterResource(R.drawable.img_streak),
                contentDescription = null,
                // 에셋 원본 비율(118:91). 이전 96x90 을 그대로 쓰면 가로가 눌린다.
                modifier = Modifier.size(width = 118.dp, height = 91.dp),
            )
        }

        // 시안 실측 27.7 (캐릭터 밑변 288.3 → 통계 블록 316).
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                caption = "모디 그룹과",
                label = "함께한지",
                value = { StatValue(number = "D+${summary.daysTogether}") },
            )
            StatDivider()
            StatColumn(
                caption = "이번달 함께한",
                label = "운동시간",
                value = {
                    StatValue(
                        number = "${summary.monthlyExerciseMinutes / 60}",
                        unit = "시간",
                    )
                },
            )
            StatDivider()
            StatColumn(
                caption = "이번달 함께한",
                label = "챌린지 개수",
                value = {
                    StatValue(
                        number = "${summary.monthlyCompletedChallengeCount}",
                        unit = "개",
                    )
                },
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatColumn(
    caption: String,
    label: String,
    value: @Composable () -> Unit,
) {
    Column(modifier = Modifier.width(80.dp)) {
        Text(
            text = caption,
            style = ModyTheme.typography.c4,
            color = ModyTheme.colors.gray06,
        )
        Text(
            text = label,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray09,
        )
        Spacer(modifier = Modifier.height(7.dp))
        value()
    }
}

/** 숫자(22 Bold) + 단위(18 SemiBold) 조합 통계 값. */
@Composable
private fun StatValue(number: String, unit: String? = null) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                ModyTheme.typography.b1.toSpanStyle().copy(fontWeight = FontWeight.Bold),
            ) { append(number) }
            if (unit != null) {
                withStyle(ModyTheme.typography.b3.toSpanStyle()) { append(unit) }
            }
        },
        style = ModyTheme.typography.b1,
        color = ModyTheme.colors.gray09,
    )
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(64.dp)
            .background(ModyTheme.colors.gray02),
    )
}

/** 회색 배경 섹션: "버디들과 신기록 도전" + 버디 카드 목록. */
@Composable
private fun BuddySection(
    buddies: List<NudgeTarget>,
    nudgingMemberIds: Set<Long>,
    onNudgeClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModyTheme.colors.gray01)
            .padding(horizontal = 24.dp)
            // 시안 실측: 배경 423~836, 내용 455~804 → 위 32 / 아래 32.
            .padding(top = 32.dp, bottom = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(ModyIcons.Medal),
                contentDescription = null,
                tint = ModyTheme.colors.gray10,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "버디들과 신기록 도전",
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (buddies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ModyTheme.colors.white),
            ) {
                buddies.forEach { buddy ->
                    BuddyRow(
                        buddy = buddy,
                        isNudging = buddy.memberId in nudgingMemberIds,
                        onNudgeClick = { onNudgeClick(buddy.memberId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BuddyRow(
    buddy: NudgeTarget,
    isNudging: Boolean,
    onNudgeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyAvatar(imageUrl = buddy.profileImageUrl, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buddy.nickname,
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
            Text(
                text = if (buddy.recordedToday) "오늘 기록을 완료했어요." else "연속기록을 위해 불러주세요!",
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.gray06,
            )
        }
        // 어떤 배지를 낼지는 서버가 정한다(buttonStatus) — 기기가 기억하던 값이 아니라서
        // 앱을 지우거나 다른 기기에서 봐도 같은 상태가 보인다.
        when (buddy.nudgeStatus) {
            NudgeButtonStatus.RECORDED -> RecordedBadge()
            NudgeButtonStatus.NUDGED -> NudgedBadge()
            NudgeButtonStatus.AVAILABLE -> NudgeButton(enabled = !isNudging, onClick = onNudgeClick)
        }
    }
}

/** 기록 완료(비활성 회색) 배지. */
@Composable
private fun RecordedBadge() {
    Box(
        modifier = Modifier
            .width(88.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ModyTheme.colors.gray03),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "기록 완료",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.white,
        )
    }
}

/** 오늘 이미 콕 찌른 상태(비활성). 하루 1회 제한이라 다시 누를 수 없다. */
@Composable
private fun NudgedBadge() {
    Box(
        modifier = Modifier
            // "기록 완료" 배지와 폭을 맞추되, 글자가 길어 88dp 를 넘으면 잘리지 않게 늘어난다.
            .widthIn(min = 88.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ModyTheme.colors.gray02)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "이미 찔렀어요",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
            maxLines = 1,
        )
    }
}

/** 콕 찌르기(옐로) 버튼. */
@Composable
private fun NudgeButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ModyTheme.colors.primary100)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            painter = painterResource(ModyIcons.Nudge),
            contentDescription = null,
            tint = ModyTheme.colors.gray10,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "콕 찌르기",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray10,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreakTabContentPreview() {
    ModyTheme {
        StreakTabContent(
            isLoading = false,
            summary = ChallengeSummary(
                daysTogether = 30,
                allMemberRecordedDays = 23,
                monthlyExerciseMinutes = 1800,
                monthlyCompletedChallengeCount = 8,
            ),
            buddies = listOf(
                // 배지 세 상태를 한 화면에서 확인.
                NudgeTarget(1, "예은", null, recordedToday = true, NudgeButtonStatus.RECORDED),
                NudgeTarget(2, "동준", null, recordedToday = false, NudgeButtonStatus.AVAILABLE),
                NudgeTarget(3, "도윤", null, recordedToday = false, NudgeButtonStatus.NUDGED),
            ),
            nudgingMemberIds = emptySet(),
            onNudgeClick = {},
        )
    }
}
