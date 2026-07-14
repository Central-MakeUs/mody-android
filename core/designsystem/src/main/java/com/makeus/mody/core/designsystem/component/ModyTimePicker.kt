package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

private val AM_PM = listOf("오전", "오후")
private val HOURS_12 = (1..12).toList()
private val MINUTES = (0..59).toList()

/**
 * 오전/오후 · 시(1~12) · 분(0~59) 3열 시간 휠 피커. 세 휠이 하나의 선택 바를 공유.
 * 온보딩 알림 시트/기록 화면에서 공용. 값은 24h 기준으로 주고받는다.
 */
@Composable
fun ModyTimePicker(
    hour24: Int,
    minute: Int,
    onTimeChange: (hour24: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
) {
    val amPmIndex = if (hour24 < 12) 0 else 1
    val hour12 = ((hour24 + 11) % 12) + 1

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // 공용 선택 바 (세 휠이 하나의 바를 공유)
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.gray01),
            )
        }
        // 간격 스펙: 오전↔시 27.5, 시↔":" 19.5, ":"↔분 19.5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelPicker(
                items = AM_PM,
                selectedIndex = amPmIndex,
                onSelectedChange = { onTimeChange(to24(it, hour12), minute) },
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = false,
                label = { it },
            )
            Spacer(modifier = Modifier.width(27.5.dp))
            WheelPicker(
                items = HOURS_12,
                selectedIndex = hour12 - 1,
                onSelectedChange = { onTimeChange(to24(amPmIndex, HOURS_12[it]), minute) },
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = false,
                loop = true,
                label = { "$it" },
            )
            Spacer(modifier = Modifier.width(19.5.dp))
            Text(
                text = ":",
                style = ModyTheme.typography.b1,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.width(19.5.dp))
            WheelPicker(
                items = MINUTES,
                selectedIndex = minute,
                onSelectedChange = { onTimeChange(hour24, MINUTES[it]) },
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = false,
                loop = true,
                label = { "%02d".format(it) },
            )
        }
    }
}

/** (오전/오후, 12h) → 24h. 오전 12시=0시, 오후 12시=12시. */
private fun to24(amPmIndex: Int, hour12: Int): Int = when {
    amPmIndex == 0 && hour12 == 12 -> 0
    amPmIndex == 0 -> hour12
    hour12 == 12 -> 12
    else -> hour12 + 12
}
