package com.makeus.mody.core.designsystem.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 스켈레톤 로딩용 shimmer 배경. 밝은 띠가 좌→우로 흐른다.
 * 데이터 로드 전 placeholder(카드/이미지/텍스트 자리)에 적용.
 */
fun Modifier.shimmer(shape: Shape = RoundedCornerShape(8.dp)): Modifier = composed {
    val base = ModyTheme.colors.gray02
    val highlight = ModyTheme.colors.gray01
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    this
        .clip(shape)
        .drawWithCache {
            val w = size.width
            // -w → +w 로 이동하는 3색 그라데이션(밝은 띠가 지나감)
            val brush = Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(x = -w + progress * 2 * w, y = 0f),
                end = Offset(x = progress * 2 * w, y = 0f),
            )
            onDrawBehind { drawRect(brush) }
        }
}
