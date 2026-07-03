package com.makeus.mody.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme
import kotlin.math.abs

/**
 * 세로 휠 피커. 가운데로 스냅되며 중앙 항목이 선택값.
 * 생년월일/체중 등 숫자 선택에 사용.
 */
@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeight: Dp = 44.dp,
    unit: String? = null,
    // false면 개별 선택박스 안 그림 (여러 휠이 하나의 공용 바를 공유할 때)
    showSelectionBox: Boolean = true,
    // true면 아이템/선택박스가 picker 폭을 꽉 채움. false면 내용 폭에 맞춰 래핑(길쭉 pill).
    fillItemWidth: Boolean = true,
    // 아이템 내용 좌우 패딩(fillItemWidth=false일 때 선택박스가 숫자보다 넓어지는 크기)
    itemHorizontalPadding: Dp = 0.dp,
    label: (T) -> String,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = state)
    val sidePadding = itemHeight * (visibleCount / 2)

    // 콜백/파생 계산이 항상 최신 prop 을 보도록 (stale capture 방지)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)

    // 뷰포트 중앙에 가장 가까운 항목 인덱스
    val centerIndex by remember {
        derivedStateOf {
            val layout = state.layoutInfo
            if (layout.visibleItemsInfo.isEmpty()) return@derivedStateOf currentSelectedIndex
            val viewportCenter =
                (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: currentSelectedIndex
        }
    }

    // 부모가 selectedIndex 를 바꾸면(예: 날짜 clamp) 휠을 해당 위치로 스크롤
    LaunchedEffect(selectedIndex) {
        if (!state.isScrollInProgress && selectedIndex != centerIndex) {
            state.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { centerIndex }.collect { idx ->
            if (idx != currentSelectedIndex) onSelectedChange(idx)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center,
    ) {
        // 중앙 선택 영역 표시. matchParentSize라 picker 폭을 강제하지 않음
        // (picker 폭은 LazyColumn 아이템이 결정 → fillItemWidth=false면 내용 폭에 맞춰 pill)
        if (showSelectionBox) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ModyTheme.colors.gray01),
                )
            }
        }

        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = sidePadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(items) { index, item ->
                val selected = index == centerIndex
                val adjacent = index == centerIndex - 1 || index == centerIndex + 1
                Box(
                    modifier = Modifier
                        .then(if (fillItemWidth) Modifier.fillMaxWidth() else Modifier)
                        .height(itemHeight)
                        .padding(horizontal = itemHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label(item),
                            // 선택: SemiBold 22sp / 위아래 비선택: Medium 18sp(b4) / 그 외: Regular 14sp(c1)
                            style = when {
                                selected -> ModyTheme.typography.b1
                                adjacent -> ModyTheme.typography.b4
                                else -> ModyTheme.typography.c1
                            },
                            color = if (selected) ModyTheme.colors.gray10 else ModyTheme.colors.gray04,
                            textAlign = TextAlign.Center,
                        )
                        // 단위는 항상 자리 차지 (선택시에만 보이게 alpha 토글) → 스크롤 중 라벨 흔들림 방지
                        if (unit != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = unit,
                                style = ModyTheme.typography.b3,
                                color = ModyTheme.colors.gray05,
                                modifier = Modifier.alpha(if (selected) 1f else 0f),
                            )
                        }
                    }
                }
            }
        }
    }
}
