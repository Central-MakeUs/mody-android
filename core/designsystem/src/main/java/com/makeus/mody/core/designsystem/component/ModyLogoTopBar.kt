package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 메인 탭 공용 상단바: MODY 로고(좌) + 알림(우).
 * 피드/마이 등 로고형 탑바가 있는 탭에서 재사용해 위치·간격 통일.
 * height 48 · 좌우 24dp. 부모가 statusBarsPadding 을 적용하면 컨텐츠가 시스템바 아래 12dp(센터정렬).
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyLogoTopBar(
    onAlarmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(ModyIcons.LogoWordmark),
            contentDescription = "MODY",
        )
        IconButton(onClick = onAlarmClick, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(ModyIcons.Alarm),
                contentDescription = "알림",
                tint = ModyTheme.colors.gray10,
            )
        }
    }
}
