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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

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
 * 시안은 반투명 흰색 + 배경 흐림(backdrop blur 15)이다. 안드로이드는 뒤 화면 흐림을 바로 그릴 수 없고,
 * 이 바는 콘텐츠 아래 별도 영역에 놓여 뒤가 늘 흰 바탕이라 불투명 흰색 + 그림자로 같은 인상을 낸다.
 */
@Composable
fun MainBottomBar(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ModyTheme.colors.white)
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = BarTopMargin, bottom = BarBottomMargin),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                // 시안 그림자: y 8, blur 6, 검정 5%.
                .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = BarShadow, spotColor = BarShadow)
                .background(ModyTheme.colors.white, CircleShape)
                .border(1.dp, ModyTheme.colors.gray01, CircleShape),
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
                    .background(ModyTheme.colors.gray01, CircleShape),
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
