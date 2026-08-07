package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.makeus.mody.core.designsystem.modifier.shimmer
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 텍스트 자리 스켈레톤의 기본 모서리. 글자 높이가 작아 카드(16)보다 덜 둥글게. */
private val TextSkeletonShape = RoundedCornerShape(4.dp)

/**
 * 값이 오기 전 자리를 잡아 두는 shimmer 박스.
 *
 * 크기를 아는 단일 값(닉네임·그룹명·숫자 등)에 쓴다. 개수를 모르는 목록은 화면별
 * 스켈레톤(예: FeedSkeletonList)에서 이 컴포저블을 조합해 만든다.
 *
 * 재조회에는 쓰지 않는다 — 이미 값이 보이는 자리를 스켈레톤이 덮으면 깜빡임이 된다.
 * 값이 아직 없는 첫 로드에만 띄운다.
 */
@Composable
fun ModySkeleton(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = TextSkeletonShape,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shimmer(shape),
    )
}

/**
 * 텍스트 한 줄 자리 스켈레톤.
 *
 * 높이를 직접 주지 않고 들어갈 자리의 타이포에서 가져온다 — 글자가 그려질 높이와
 * 스켈레톤 높이가 어긋나면 값이 도착하는 순간 주변 요소가 위아래로 튄다.
 *
 * @param width 글자 수를 가늠한 폭.
 * @param lineHeight 그 자리에 들어갈 TextStyle 의 lineHeight.
 */
@Composable
fun ModyTextSkeleton(
    width: Dp,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
) {
    // sp → dp: 글꼴 크기 배율까지 반영해야 실제 줄 높이와 맞는다.
    val height = with(LocalDensity.current) {
        if (lineHeight.isSpecified) lineHeight.toDp() else 16.dp
    }
    ModySkeleton(width = width, height = height, modifier = modifier)
}

/** 아바타 자리 스켈레톤(원형). */
@Composable
fun ModyAvatarSkeleton(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(size).shimmer(CircleShape))
}

@Preview(showBackground = true)
@Composable
private fun ModySkeletonPreview() {
    ModyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModyTextSkeleton(width = 120.dp, lineHeight = ModyTheme.typography.b2.lineHeight)
            ModyTextSkeleton(width = 72.dp, lineHeight = ModyTheme.typography.c2.lineHeight)
            ModyAvatarSkeleton(size = 40.dp)
        }
    }
}
