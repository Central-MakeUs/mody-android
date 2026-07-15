package com.makeus.mody.feature.record.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 기록 화면 섹션 라벨: 24dp 아이콘 + b7/gray08 텍스트. */
@Composable
fun SectionHeader(
    @DrawableRes icon: Int,
    label: String,
    iconSpacing: Dp = 8.dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ModyTheme.colors.gray08,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(iconSpacing))
        Text(
            text = label,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray08,
        )
    }
}
