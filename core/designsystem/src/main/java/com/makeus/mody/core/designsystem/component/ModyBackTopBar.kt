package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 뒤로가기 탑바: 뒤로가기 "<" + (선택)아바타 + 타이틀.
 * 상태바 바로 아래 붙지 않게 상단 12dp 여백 내장 → 부모는 statusBarsPadding 만 적용.
 * 백키가 있는 화면(기록/알림/상세 등)에서 재사용해 위치·간격 통일.
 *
 * @param avatarUrl null 이 아니면(또는 [showAvatar]) 뒤로가기와 타이틀 사이 32dp 아바타 노출.
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyBackTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAvatar: Boolean = false,
    avatarUrl: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(56.dp)
            .padding(start = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModyBackButton(onClick = onBackClick)
        if (showAvatar) {
            ModyAvatar(imageUrl = avatarUrl, size = 32.dp)
        }
        Text(
            text = title,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
        )
    }
}
