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

private val HOURS = (0..23).toList()
private val MINUTES = (0..59).toList()

/**
 * 운동 시간 등 "지속 시간" 선택용 2열 휠 피커. 시(0~23) · 분(0~59) 이 하나의 선택 바를 공유.
 * 시각(오전/오후)이 아니라 duration 이라 [ModyTimePicker] 와 분리.
 *
 * "시간"/"분" 라벨은 스크롤되지 않는 정적 라벨로, 휠 컬럼 가장자리에서 [labelSpacing] 만큼 띄운다
 * (시안: 휠↔라벨 4dp, 그룹↔":" 8dp).
 */
@Composable
fun ModyDurationPicker(
    hours: Int,
    minutes: Int,
    onChange: (hours: Int, minutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 39.dp,
    selectionBarHeight: Dp = 34.dp,
    selectionBarCornerRadius: Dp = 8.dp,
    hourWheelWidth: Dp = 30.dp,
    minuteWheelWidth: Dp = 30.dp,
    labelSpacing: Dp = 4.dp,
    groupSpacing: Dp = 8.dp,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // 공용 선택 바 (두 휠이 하나의 바를 공유)
        Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(selectionBarHeight)
                    .clip(RoundedCornerShape(selectionBarCornerRadius))
                    .background(ModyTheme.colors.gray01),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelPicker(
                items = HOURS,
                selectedIndex = hours.coerceIn(0, HOURS.lastIndex),
                onSelectedChange = { onChange(HOURS[it], minutes) },
                modifier = Modifier.width(hourWheelWidth),
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = true,
                loop = true,
                label = { "$it" },
            )
            Spacer(modifier = Modifier.width(labelSpacing))
            DurationLabel("시간")
            Spacer(modifier = Modifier.width(groupSpacing))
            Text(
                text = ":",
                style = ModyTheme.typography.b1,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.width(groupSpacing))
            WheelPicker(
                items = MINUTES,
                selectedIndex = minutes.coerceIn(0, MINUTES.lastIndex),
                onSelectedChange = { onChange(hours, MINUTES[it]) },
                modifier = Modifier.width(minuteWheelWidth),
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = true,
                loop = true,
                label = { "%02d".format(it) },
            )
            Spacer(modifier = Modifier.width(labelSpacing))
            DurationLabel("분")
        }
    }
}

/** 스크롤되지 않는 단위 라벨(시안: SemiBold 16 / gray05). */
@Composable
private fun DurationLabel(text: String) {
    Text(
        text = text,
        style = ModyTheme.typography.b6,
        color = ModyTheme.colors.gray05,
    )
}
