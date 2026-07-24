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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

private val HOURS = (0..6).toList()
private val MINUTES = (0..59).toList()

/**
 * 운동 시간 등 "지속 시간" 선택용 2열 휠 피커. 시(0~23) · 분(0~59) 이 하나의 선택 바를 공유.
 * 시각(오전/오후)이 아니라 duration 이라 [ModyTimePicker] 와 분리.
 *
 * "시간"/"분" 라벨은 스크롤되지 않는 정적 라벨. 간격은 시안 실측(글리프 기준)이라
 * 휠 컬럼을 고정 폭이 아니라 내용 폭에 맞춰(fillItemWidth=false) 감싸야 값이 그대로 맞는다:
 *
 *     [시] ─11.5─ "시간" ─19.5─ ":" ─18─ [분] ─9.5─ "분"
 *
 * 시는 항상 1자리, 분은 "%02d" 로 항상 2자리라 컬럼 폭이 스크롤 중에도 흔들리지 않는다.
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
    hourToHourLabel: Dp = 11.5.dp,
    hourLabelToColon: Dp = 19.5.dp,
    colonToMinute: Dp = 18.dp,
    minuteToMinuteLabel: Dp = 9.5.dp,
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
                // 최대 시(6) 선택 시 분은 00 으로 고정 → 상한 6시간 00분
                onSelectedChange = {
                    val h = HOURS[it]
                    onChange(h, if (h >= HOURS.last()) 0 else minutes)
                },
                itemHeight = itemHeight,
                showSelectionBox = false,
                fillItemWidth = false,
                loop = true,
                label = { "$it" },
            )
            Spacer(modifier = Modifier.width(hourToHourLabel))
            DurationLabel("시간")
            Spacer(modifier = Modifier.width(hourLabelToColon))
            Text(
                text = ":",
                style = ModyTheme.typography.b1,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.width(colonToMinute))
            // 상한 6시간 00분: 최대 시(6)에선 분 항목을 00 하나로 제한.
            // (스냅백 방식은 state 가 이미 0 이면 selectedIndex 변화가 없어 휠이 안 돌아옴)
            val minuteItems = if (hours >= HOURS.last()) listOf(0) else MINUTES
            key(minuteItems.size) {
                WheelPicker(
                    items = minuteItems,
                    selectedIndex = minutes.coerceIn(0, minuteItems.lastIndex),
                    onSelectedChange = { onChange(hours, minuteItems[it]) },
                    itemHeight = itemHeight,
                    showSelectionBox = false,
                    fillItemWidth = false,
                    loop = true,
                    label = { "%02d".format(it) },
                )
            }
            Spacer(modifier = Modifier.width(minuteToMinuteLabel))
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
