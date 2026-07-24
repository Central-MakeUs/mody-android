package com.makeus.mody.core.designsystem.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 아이콘 버튼용 원형 ripple clickable.
 *
 * 작은 아이콘 Box 에 기본 [clickable] 을 쓰면 clip 안 된 사각 경계로 ripple 이 각지게 튀어
 * 아이콘 딱 맞는 네모 splash 처럼 보인다. unbounded 원형 ripple 로 통일해 자연스럽게 퍼지게 한다.
 *
 * @param radius 원형 ripple 반경. 아이콘(보통 24dp)보다 살짝 크게 잡아 밖으로 부드럽게 번진다.
 */
fun Modifier.iconRippleClickable(
    radius: Dp = 20.dp,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = radius),
        onClick = onClick,
    )
}
