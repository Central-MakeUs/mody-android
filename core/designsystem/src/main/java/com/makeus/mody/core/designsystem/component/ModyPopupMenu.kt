package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 화면 좌우 여백. 팝업 메뉴는 이 안쪽에서만 뜬다(화면 본문과 같은 세로선에 맞춘다). */
private val ScreenHorizontalMargin = 24.dp

/** 그림자가 팝업 창 경계에 잘리지 않도록 남기는 여백. 위치 계산에서 상쇄한다. */
private val ShadowInset = 6.dp

private val MenuShape = RoundedCornerShape(8.dp)

/**
 * 공용 팝업 메뉴(시안: 기록 ⋯ 메뉴 9:3503, 댓글 꾹 누르기 9:3713).
 *
 * 머티리얼 DropdownMenu 대신 직접 그린다. DropdownMenu 는 위치가 앵커 왼쪽 기준이라 오른쪽 끝의
 * ⋯ 에서 열면 화면 끝에 붙거나 여백이 화면마다 달라지고, 안쪽 위아래 여백(8dp)도 시안과 다르다.
 *
 * 위치 규칙
 * - 메뉴 오른쪽 끝 = 앵커 오른쪽 끝([alignEnd] false 면 왼쪽 끝끼리). 메뉴 위쪽 = 앵커 아래쪽 + [offsetY].
 * - 어떤 경우에도 화면 좌우 24dp 안쪽에 들어오게 민다 — 화면 본문 여백과 같은 선.
 * - 아래 공간이 모자라면 앵커 위로 뒤집는다.
 *
 * 메뉴를 띄울 요소를 `Box { 앵커; ModyPopupMenu(...) }` 로 감싸 쓴다.
 *
 * @param offsetY 앵커 아래쪽과의 간격. 말풍선에 살짝 겹치게 하려면 음수.
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    alignEnd: Boolean = true,
    offsetY: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val positionProvider = remember(density, alignEnd, offsetY) {
        with(density) {
            AnchoredMenuPositionProvider(
                marginPx = ScreenHorizontalMargin.roundToPx(),
                insetPx = ShadowInset.roundToPx(),
                offsetYPx = offsetY.roundToPx(),
                alignEnd = alignEnd,
            )
        }
    }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Box(modifier = Modifier.padding(ShadowInset)) {
            Column(
                modifier = Modifier
                    // 시안 Mody_shadow: #8D8D8D 25%, y 2, blur 8.
                    .shadow(
                        elevation = 4.dp,
                        shape = MenuShape,
                        ambientColor = MenuShadowColor,
                        spotColor = MenuShadowColor,
                    )
                    .clip(MenuShape)
                    .background(ModyTheme.colors.white)
                    .border(1.dp, ModyTheme.colors.gray01, MenuShape),
                content = content,
            )
        }
    }
}

private val MenuShadowColor = Color(0x408D8D8D)

/**
 * 팝업 메뉴 항목. 최소 88x34, 가운데 정렬 C1.
 * 신고·삭제처럼 되돌리기 어려운 동작이 기본이라 기본 색은 error 다.
 */
@Suppress("unused")
@Composable
fun ModyPopupMenuItem(
    text: String,
    onClick: () -> Unit,
    color: Color = ModyTheme.colors.error,
) {
    Box(
        modifier = Modifier
            .widthIn(min = 88.dp)
            .height(34.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = ModyTheme.typography.c1, color = color)
    }
}

private class AnchoredMenuPositionProvider(
    private val marginPx: Int,
    private val insetPx: Int,
    private val offsetYPx: Int,
    private val alignEnd: Boolean,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        // 팝업 창은 그림자 여백(insetPx)만큼 크다. 보이는 메뉴 상자 기준으로 계산하고 창 위치로 되돌린다.
        val menuWidth = popupContentSize.width - insetPx * 2
        val menuHeight = popupContentSize.height - insetPx * 2
        val preferredLeft = if (alignEnd) anchorBounds.right - menuWidth else anchorBounds.left
        val maxLeft = (windowSize.width - marginPx - menuWidth).coerceAtLeast(marginPx)
        val left = preferredLeft.coerceIn(marginPx, maxLeft)

        val below = anchorBounds.bottom + offsetYPx
        val top = if (below + menuHeight <= windowSize.height) {
            below
        } else {
            (anchorBounds.top - offsetYPx - menuHeight).coerceAtLeast(0)
        }
        return IntOffset(left - insetPx, top - insetPx)
    }
}
