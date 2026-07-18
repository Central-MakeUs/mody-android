package com.makeus.mody.feature.feed.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.feed.contract.WeekDayUi
import java.time.LocalDate

/**
 * 주차 헤더("7월 2주차" + 이전/다음 주) + 주간 날짜 스트립 (Feed_기본 시안).
 * 하단 1dp gray02 구분선 포함.
 */
@Composable
fun FeedWeekSection(
    weekLabel: String,
    weekDays: List<WeekDayUi>,
    canGoNextWeek: Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WeekHeader(
            label = weekLabel,
            canGoNextWeek = canGoNextWeek,
            onPrevWeek = onPrevWeek,
            onNextWeek = onNextWeek,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 16.dp),
        ) {
            weekDays.forEach { day ->
                WeekDayCell(
                    day = day,
                    onClick = { onDaySelected(day.date) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
    }
}

@Composable
private fun WeekHeader(
    label: String,
    canGoNextWeek: Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray06,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onPrevWeek, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(ModyIcons.Left),
                    contentDescription = "이전 주",
                    tint = ModyTheme.colors.gray06,
                )
            }
            IconButton(
                onClick = onNextWeek,
                enabled = canGoNextWeek,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    // 좌우 대칭이 정확하도록 이전(<) 아이콘을 미러링해 다음(>)으로 사용.
                    painter = painterResource(ModyIcons.Left),
                    contentDescription = "다음 주",
                    // 이번 주(미래 이동 불가)면 비활성 색.
                    tint = if (canGoNextWeek) ModyTheme.colors.gray06 else ModyTheme.colors.gray03,
                    modifier = Modifier.graphicsLayer { scaleX = -1f },
                )
            }
        }
    }
}

@Composable
private fun WeekDayCell(
    day: WeekDayUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = !day.isFuture, // 미래 날짜는 선택 불가 (불러올 기록 없음)
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = day.weekdayLabel,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray06,
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (day.isSelected) ModyTheme.colors.primary100 else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${day.date.dayOfMonth}",
                style = ModyTheme.typography.b7,
                color = if (day.isFuture) ModyTheme.colors.gray03 else ModyTheme.colors.gray10,
            )
        }
        // 미래 날짜는 점 표시 없음. 그 외엔 기록 유무에 따라 색.
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isFuture -> Color.Transparent
                        day.hasFeed -> ModyTheme.colors.secondary100
                        else -> ModyTheme.colors.gray02
                    },
                ),
        )
    }
}
