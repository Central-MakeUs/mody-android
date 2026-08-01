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

/** "N일째 전원 연속 기록 완료!" + 축하 캐릭터 + 통계 3열. */
@Composable
private fun StreakHeader(summary: ChallengeSummary?) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${summary?.allMemberRecordedDays ?: 0}",
                        style = ModyTheme.typography.h1.copy(fontSize = 36.sp, lineHeight = 50.4.sp),
                        color = ModyTheme.colors.gray09,
                    )
                    Text(
                        text = "일째",
                        style = ModyTheme.typography.h1,
                        color = ModyTheme.colors.gray09,
                    )
                }
                Text(
                    text = "전원 연속 기록 완료!",
                    style = ModyTheme.typography.b2,
                    color = ModyTheme.colors.gray09,
                )
            }
            Image(
                painter = painterResource(R.drawable.img_challenge_celebrate),
                contentDescription = null,
                modifier = Modifier.size(width = 96.dp, height = 90.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                caption = "모디 그룹과",
                label = "함께한지",
                value = { StatValue(number = "D+${summary?.daysTogether ?: 0}") },
            )
            StatDivider()
            StatColumn(
                caption = "이번달 함께한",
                label = "운동시간",
                value = {
                    StatValue(
                        number = "${(summary?.monthlyExerciseMinutes ?: 0) / 60}",
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
                        number = "${summary?.monthlyCompletedChallengeCount ?: 0}",
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
            .padding(top = 32.dp, bottom = 24.dp),
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
        if (buddy.recordedToday) {
            RecordedBadge()
        } else {
            NudgeButton(enabled = !isNudging, onClick = onNudgeClick)
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
                NudgeTarget(1, "예은", null, recordedToday = true),
                NudgeTarget(2, "동준", null, recordedToday = false),
                NudgeTarget(3, "도윤", null, recordedToday = true),
            ),
            nudgingMemberIds = emptySet(),
            onNudgeClick = {},
        )
    }
}
