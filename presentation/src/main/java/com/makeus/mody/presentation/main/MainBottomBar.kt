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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
                // 시안: 흰색 20% + 배경 흐림 15. 흰 화면 위에선 20% 면 거의 안 보여 흐림을 조금 키우고
                // 흰색을 살짝 더 얹는다(아래 BarTint).
                // backgroundColor 는 필수 — 흐림 가장자리를 채울 바탕색. 없으면 그리는 순간 예외로 죽는다.
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.White,
                        tint = HazeTint(BarTint),
                        blurRadius = BarBlur,
                    ),
                )
                // 시안 안쪽 하이라이트(흰 테두리 빛).
                .border(1.dp, BarHighlight, CircleShape),
        ) {
            val cellWidth = (maxWidth - IndicatorInset * 2) / tabs.size.coerceAtLeast(1)
            val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)
            val indicatorOffset by animateDpAsState(
                targetValue = IndicatorInset + cellWidth * selectedIndex,
                label = "tabIndicator",
            )
            // 선택 표시(안쪽 알약).
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset, y = IndicatorInset)
                    .width(cellWidth)
                    .height(BarHeight - IndicatorInset * 2)
                    // 시안: 안쪽 알약도 흰색 20% 글래스 — 바 위에 한 겹 더 밝게 얹는다.
                    .background(IndicatorTint, CircleShape)
                    .border(1.dp, BarHighlight, CircleShape),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = IndicatorInset),
            ) {
                tabs.forEach { tab ->
                    TabItem(
                        tab = tab,
                        isSelected = tab == selected,
                        onClick = { onSelect(tab) },
                    )
                }
            }
        }
    }
}

private val BarShadow = Color(0x0D000000)
private val BarTint = Color.White.copy(alpha = 0.55f)
private val IndicatorTint = Color.White.copy(alpha = 0.6f)
private val BarHighlight = Color.White.copy(alpha = 0.6f)
private val BarBlur = 20.dp

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
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
