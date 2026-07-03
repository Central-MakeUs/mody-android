package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 뒤로가기 버튼 히트영역(접근성 권장 최소 터치 타깃). */
val ModyBackButtonSize = 48.dp

/**
 * 상단 뒤로가기 "<" 버튼. 48dp 히트영역(접근성) 안에 10x17 ic_chevron_left 배치.
 */
@Composable
fun ModyBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ModyTheme.colors.gray10,
    contentDescription: String = "뒤로가기",
) {
    Box(
        modifier = modifier
            .size(ModyBackButtonSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .width(10.dp)
                .height(17.dp),
        )
    }
}
