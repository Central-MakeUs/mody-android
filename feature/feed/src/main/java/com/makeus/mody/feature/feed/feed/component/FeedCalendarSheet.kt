package com.makeus.mody.feature.feed.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.feed.contract.CalendarDayUi

// TODO(designsystem): 피드 있는 날 점 색 토큰 확정 시 교체
private val FeedDot = Color(0xFF7B8BF5)

private val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

/** 날짜 선택 캘린더 바텀시트 (Feed3 시안). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCalendarSheet(
    title: String,
    days: List<CalendarDayUi>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ModyTheme.colors.white,
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // 헤더: < 2026.07 > + 닫기
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(ModyIcons.Left),
                            contentDescription = "이전 달",
                            tint = ModyTheme.colors.gray10,
                        )
                    }
                    Text(
                        text = title,
                        style = ModyTheme.typography.b3,
                        color = ModyTheme.colors.gray10,
                    )
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(ModyIcons.Right),
                            contentDescription = "다음 달",
                            tint = ModyTheme.colors.gray10,
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(ModyIcons.Clear),
                        contentDescription = "닫기",
                        tint = ModyTheme.colors.gray10,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 요일 헤더
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_LABELS.forEach { label ->
                    Text(
                        text = label,
                        style = ModyTheme.typography.c1,
                        color = ModyTheme.colors.gray05,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 날짜 그리드 (7열)
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        DayCell(
                            cell = cell,
                            onClick = { if (cell.inMonth) onDaySelected(cell.day) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            ModyButton(
                text = "확인",
                onClick = onConfirm,
                variant = ModyButtonVariant.Primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun DayCell(
    cell: CalendarDayUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = cell.inMonth, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (cell.isToday) ModyTheme.colors.primary100 else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (cell.day > 0) {
                Text(
                    text = "${cell.day}",
                    style = ModyTheme.typography.b6,
                    color = if (cell.inMonth) ModyTheme.colors.gray10 else ModyTheme.colors.gray04,
                )
            }
        }
        when {
            cell.isToday -> Text(
                text = "오늘",
                style = ModyTheme.typography.c3,
                color = ModyTheme.colors.gray10,
            )
            cell.hasFeed -> Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(FeedDot),
            )
        }
    }
}
