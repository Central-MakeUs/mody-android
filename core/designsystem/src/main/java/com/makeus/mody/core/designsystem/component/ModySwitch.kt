package com.makeus.mody.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 켜짐 트랙 색(시안 그린). 전용 토큰이 없어 컴포넌트 로컬 상수로 둠. */
private val SwitchOnGreen = Color(0xFF4CD964)

private val TrackWidth = 56.dp
private val TrackHeight = 28.dp
private val ThumbWidth = 34.dp
private val ThumbHeight = 24.dp
private val ThumbPadding = 2.dp

/**
 * 공용 온/오프 스위치. 켜짐=그린 트랙, 꺼짐=gray02 트랙, 흰색 thumb.
 * 상태는 호출 측이 소유(stateless) — [checked]/[onCheckedChange].
 */
@Composable
fun ModySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) SwitchOnGreen else ModyTheme.colors.gray02,
        label = "switchTrack",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) TrackWidth - ThumbWidth - ThumbPadding else ThumbPadding,
        label = "switchThumb",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = TrackWidth, height = TrackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(width = ThumbWidth, height = ThumbHeight)
                .clip(CircleShape)
                .background(ModyTheme.colors.white),
        )
    }
}
