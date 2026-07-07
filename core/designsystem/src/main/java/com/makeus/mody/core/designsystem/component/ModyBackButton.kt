package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 상단 뒤로가기 "<" 버튼.
 * Material3 IconButton 사용 → 48dp 터치 타깃 + ripple + 버튼 semantics 기본 제공.
 * 아이콘은 10x17 ic_chevron_left.
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ModyTheme.colors.gray10,
    contentDescription: String = "뒤로가기",
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .width(10.dp)
                .height(17.dp),
        )
    }
}
