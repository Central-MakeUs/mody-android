package com.makeus.mody.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * [MainBottomBar] 콘텐츠 높이 (상단 보더 1dp + 탭 48dp). 시스템 내비게이션 인셋은 별도.
 * 피드 FAB 등 바 위에 떠야 하는 요소가 겹치지 않게 띄울 때 사용.
 */
val MainBottomBarContentHeight = 49.dp

/**
 * 메인 하단 네비게이션 바 (시안: Bottom navigation).
 * 상단 1dp 보더(gray02) + 48dp 바 + 시스템 내비게이션 인셋.
 * 선택 탭: fill 아이콘 + c2/gray10, 미선택: outline 아이콘 + c3/gray05.
 */
@Composable
fun MainBottomBar(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ModyTheme.colors.white)
            .navigationBarsPadding(),
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray02),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (isSelected) ModyTheme.colors.gray10 else ModyTheme.colors.gray05

    Column(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 시안에 리플 없음
                onClick = onClick,
            )
            .padding(top = 5.5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(if (isSelected) tab.selectedIcon else tab.icon),
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = tab.label,
            style = if (isSelected) ModyTheme.typography.c3 else ModyTheme.typography.c4,
            color = contentColor,
        )
    }
}
