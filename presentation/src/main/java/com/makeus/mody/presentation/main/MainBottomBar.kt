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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.shadow
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

/** 바 위·아래 여백. 바는 화면 아래에 떠 있는 알약 모양이라 위아래로 띄운다. */
private val BarTopMargin = 8.dp
private val BarBottomMargin = 12.dp

/** 선택 표시(안쪽 알약)와 바 테두리 사이 간격(시안 6). */
private val IndicatorInset = 6.dp

/**
 * [MainBottomBar] 가 차지하는 높이(시스템 내비게이션 인셋 제외).
 * 피드 FAB 등 바 위에 떠야 하는 요소가 겹치지 않게 띄울 때 사용.
 */
val MainBottomBarContentHeight = BarTopMargin + BarHeight + BarBottomMargin

/**
 * 메인 하단 탭 바(시안 menu 35:3564).
 *
 * 좌우 24 여백의 알약 모양 바 안에서, 선택 탭 뒤에 안쪽 알약(선택 표시)이 깔리고 탭을 바꾸면 미끄러진다.
 * 선택 탭은 채운 아이콘 + C3, 나머지는 선 아이콘 + C4. 색은 모두 gray10.
 *
 * 콘텐츠 위에 떠서 뒤를 흐리게 비춘다(Haze — [MainScreen] 의 콘텐츠가 원본). 배경 흐림은 API 31+ 에서만
 * 되고, 그 아래는 같은 반투명 흰색만 그려진다.
 *
 * 시안과 다른 점: 바 아래 여백. 시안은 28 이지만 시스템 내비게이션 바 위로 28 을 더 띄우면 너무 올라가
 * [BarBottomMargin](12)만 둔다.
 */
@Composable
fun MainBottomBar(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 바 바깥은 투명 — 콘텐츠가 바 양옆·아래로 그대로 보인다.
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = BarTopMargin, bottom = BarBottomMargin),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                // 시안 그림자: y 8, blur 6, 검정 5%.
                .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = BarShadow, spotColor = BarShadow)
                .clip(CircleShape)
                // 시안: 흰색 20% + 배경 흐림 15.
                // backgroundColor 는 필수 — 흐림 가장자리를 채울 바탕색. 없으면 그리는 순간 예외로 죽는다.
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.White,
                        tint = HazeTint(GlassTint),
                        blurRadius = GlassBlur,
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
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = BarShadow, spotColor = BarShadow)
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
 * 시안 안쪽 빛(inset shadow 3겹: 위·왼쪽 흰 60%, 위아래 가장자리 흰 25%)을 테두리 한 줄로 근사한다.
 * Compose 엔 inset shadow 가 없어, 위쪽이 밝고 아래로 옅어지는 흰 그라데이션 테두리로 같은 인상을 낸다.
 */
private fun Modifier.glassHighlight(): Modifier = border(
    width = 1.dp,
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.25f))),
    shape = CircleShape,
)

private val BarShadow = Color(0x0D000000)
/** 시안 글래스: 흰색 20% + 배경 흐림 15. 바와 선택 표시가 같은 값을 쓴다. */
private val GlassTint = Color.White.copy(alpha = 0.2f)
private val GlassBlur = 15.dp

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
