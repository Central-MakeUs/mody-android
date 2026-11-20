package com.makeus.mody.presentation.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

/** 바 높이(시안 58). */
private val BarHeight = 58.dp

/** 바 위 여백(스크롤 끝 여백 계산용). */
private val BarTopMargin = 8.dp

/** 버튼 내비게이션(◁ ○ ▢)일 때 그 위로 띄우는 간격. 시안 28 을 버튼 위로 더하면 너무 올라간다. */
private val BarMarginAboveNavButtons = 12.dp

/** 제스처 내비게이션(또는 없음)일 때 화면 맨 아래에서 바까지(시안 28). */
private val BarMarginFromScreenBottom = 28.dp

/** FAB 와 바 사이 간격. */
private val FabGapAboveBar = 12.dp

/** 선택 표시(안쪽 알약)와 바 테두리 사이 간격(시안 6). */
private val IndicatorInset = 6.dp

/**
 * 하단 탭 바 배치. 시스템 내비게이션 방식에 따라 바 높이가 달라져, 바·스크롤 여백·FAB 가 같은 값을 쓴다.
 *
 * @param barBottom 화면 맨 아래 → 바 아래쪽 거리.
 * @param contentInset 탭 콘텐츠가 스크롤 끝에 둘 여백(화면 맨 아래 → 바 위쪽 + 바 위 여백).
 * @param fabBottomPadding FAB 가 시스템 내비게이션 인셋 위로 띄울 거리(FAB 는 인셋을 따로 더한다).
 */
data class MainBottomBarLayout(val barBottom: Dp, val contentInset: Dp, val fabBottomPadding: Dp)

/**
 * - 버튼 내비게이션: 버튼 줄 위로 [BarMarginAboveNavButtons].
 * - 제스처 내비게이션(또는 없음): 화면 맨 아래에서 [BarMarginFromScreenBottom](시안 28). 제스처 핸들
 *   인셋이 그보다 크면 핸들 위로.
 *
 * 둘은 tappableElement 인셋으로 가른다 — 버튼은 누를 수 있는 영역이라 값이 있고, 제스처 핸들은 0 이다.
 */
@Composable
fun rememberMainBottomBarLayout(): MainBottomBarLayout {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val tappableBottom = WindowInsets.tappableElement.asPaddingValues().calculateBottomPadding()
    val barBottom = if (tappableBottom > 0.dp) {
        navBottom + BarMarginAboveNavButtons
    } else {
        maxOf(BarMarginFromScreenBottom, navBottom)
    }
    return MainBottomBarLayout(
        barBottom = barBottom,
        contentInset = barBottom + BarHeight + BarTopMargin,
        fabBottomPadding = barBottom + BarHeight + FabGapAboveBar - navBottom,
    )
}

/**
 * 메인 하단 탭 바(시안 menu 35:3564).
 *
 * 좌우 24 여백의 알약 모양 바 안에서, 선택 탭 뒤에 안쪽 알약(선택 표시)이 깔리고 탭을 바꾸면 미끄러진다.
 * 선택 탭은 채운 아이콘 + C3, 나머지는 선 아이콘 + C4. 색은 모두 gray10.
 *
 * 콘텐츠 위에 떠서 뒤를 흐리게 비춘다(Haze — [MainScreen] 의 콘텐츠가 원본). 배경 흐림은 API 31+ 에서만
 * 되고, 그 아래는 같은 반투명 흰색만 그려진다.
 *
 * 바 아래 여백은 시스템 내비게이션 방식에 따라 다르다([rememberMainBottomBarLayout]).
 */
@Composable
fun MainBottomBar(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    hazeState: HazeState,
    layout: MainBottomBarLayout,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 바 바깥은 투명 — 콘텐츠가 바 양옆·아래로 그대로 보인다.
            .padding(start = 24.dp, end = 24.dp, bottom = layout.barBottom),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .glassDropShadow()
                .clip(CircleShape)
                // backgroundColor 는 필수 — 흐림 가장자리를 채울 바탕색. 없으면 그리는 순간 예외로 죽는다.
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.White,
                        tint = HazeTint(BarTint),
                        blurRadius = BarBlur,
                    ),
                )
                .glassHighlight(),
        ) {
            // 시안 좌표(바 폭 354 기준)를 비율로 옮긴다 — 화면 폭이 달라도 같은 자리에 놓이게.
            val scale = maxWidth / DESIGN_BAR_WIDTH
            val indicatorWidth = DESIGN_INDICATOR_WIDTH * scale
            val centers = tabCenters(tabs.size).map { it * scale }
            val slotWidth = if (centers.size > 1) centers[1] - centers[0] else maxWidth
            val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)
            val indicatorLeft by animateDpAsState(
                targetValue = (centers.getOrElse(selectedIndex) { maxWidth / 2 }) - indicatorWidth / 2,
                label = "tabIndicator",
            )
            // 선택 표시(안쪽 알약). 시안: 흰색 20% 글래스 한 겹 + 같은 안쪽 빛.
            Box(
                modifier = Modifier
                    .offset(x = indicatorLeft, y = IndicatorInset)
                    .width(indicatorWidth)
                    .height(BarHeight - IndicatorInset * 2)
                    .glassDropShadow()
                    .background(GlassTint, CircleShape)
                    .glassHighlight(),
            )
            tabs.forEachIndexed { index, tab ->
                TabItem(
                    tab = tab,
                    isSelected = tab == selected,
                    onClick = { onSelect(tab) },
                    modifier = Modifier
                        .offset(x = centers[index] - slotWidth / 2)
                        .width(slotWidth)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

/**
 * 시안 탭 중심 x(바 폭 354 기준). 피드 69.5 · 챌린지 177 · 마이 284.5 — 양 끝 탭은 바 끝에서 69.5 안쪽,
 * 그 사이는 같은 간격. 탭 수가 다르면 같은 양 끝 사이에 고르게 놓는다.
 */
private fun tabCenters(count: Int): List<Dp> = when (count) {
    0 -> emptyList()
    1 -> listOf(DESIGN_BAR_WIDTH / 2)
    else -> {
        val first = DESIGN_EDGE_CENTER
        val last = DESIGN_BAR_WIDTH - DESIGN_EDGE_CENTER
        List(count) { i -> first + (last - first) * i / (count - 1) }
    }
}

/**
 * 시안 그림자(drop shadow: x 0, y 8, blur 6, 검정 5%)를 피그마처럼 그린다.
 *
 * Compose `shadow()` 는 시스템 elevation 그림자라 아래로 내리는 거리를 줄 수 없고, 색 알파가 시스템 그림자
 * 농도와 곱해져 5% 가 사실상 안 보인다 — 흰 화면에서 바가 떠 보이지 않던 이유다. 그래서 그림자 레이어를
 * 직접 그린다. 바 안쪽은 잘라내 반투명 글래스 밑에 그림자가 비치지 않게 한다(CSS box-shadow 와 같다).
 * API 28 미만은 하드웨어 가속에서 그림자 레이어가 안 그려져 그림자만 빠진다.
 */
private fun Modifier.glassDropShadow(): Modifier = drawBehind {
    val radius = size.height / 2
    val shape = Path().apply {
        addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius)))
    }
    clipPath(shape, clipOp = ClipOp.Difference) {
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = ShadowColor.toArgb()
                setShadowLayer(ShadowBlur.toPx(), 0f, ShadowOffsetY.toPx(), ShadowColor.toArgb())
            }
            canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)
        }
    }
}

/**
 * 시안 안쪽 빛(inset shadow 3겹: 위·왼쪽 흰 60%, 위아래 가장자리 흰 25%)을 테두리 한 줄로 근사한다.
 * Compose 엔 inset shadow 가 없어, 위쪽이 밝고 아래로 옅어지는 흰 그라데이션 테두리로 같은 인상을 낸다.
 */
private fun Modifier.glassHighlight(): Modifier = border(
    width = 1.dp,
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.25f))),
    shape = CircleShape,
)

/** 시안 drop shadow: y 8, blur 6, 검정 5%. */
private val ShadowColor = Color(0x0D000000)
private val ShadowOffsetY = 8.dp
private val ShadowBlur = 6.dp
/**
 * 바 글래스: 흰색 40% + 흐림 24.
 *
 * 시안은 흰색 20% + 흐림 15 인데, 그러면 뒤 색이 80% 비쳐 검은 사진 위에서 바가 어두워지고 검은 아이콘·
 * 글자가 묻힌다. 흐림을 키워 뒤 사진을 색 덩어리로만 비치게 하고(글래스 느낌 유지), 흰색을 40% 로 올려
 * 비치는 느낌과 글자 대비 사이에서 맞춘다(기기에서 60% 는 너무 하얘 40% 로 정함).
 * 흰 화면에서는 시안과 같게 보인다.
 */
private val BarTint = Color.White.copy(alpha = 0.4f)
private val BarBlur = 24.dp

/** 선택 표시(안쪽 알약): 시안대로 흰색 20% 한 겹을 바 위에 더 얹는다. */
private val GlassTint = Color.White.copy(alpha = 0.2f)

/** 시안 좌표계(바 폭 354)의 값들. */
private val DESIGN_BAR_WIDTH = 354.dp
private val DESIGN_INDICATOR_WIDTH = 127.dp
private val DESIGN_EDGE_CENTER = 69.5.dp

@Composable
private fun TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 시안에 리플 없음. 선택 표시가 미끄러지는 것으로 피드백.
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(if (isSelected) tab.selectedIcon else tab.icon),
            contentDescription = tab.label,
            tint = ModyTheme.colors.gray10,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = tab.label,
            style = if (isSelected) ModyTheme.typography.c3 else ModyTheme.typography.c4,
            color = ModyTheme.colors.gray10,
        )
    }
}
