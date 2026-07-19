package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 뒤로가기 탑바: 뒤로가기 "<" + (선택)아바타 + 타이틀.
 * Figma 상단바는 로고형/백키형이 같은 컴포넌트 → [ModyLogoTopBar] 와 동일하게
 * height 48 · 좌우 24dp · 아이콘 24dp. 상태바 여백은 부모가 statusBarsPadding 으로 처리.
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
            .height(48.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ModyBackButton(onClick = onBackClick, modifier = Modifier.size(24.dp))
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
