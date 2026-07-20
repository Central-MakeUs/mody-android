package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 뒤로가기 탑바 (Figma 스펙: height 48, padding 12/24, space-between).
 * 3가지 변형을 한 컴포넌트로:
 *  - 뒤로만
 *  - 뒤로 + (우측)닫기 X  ([onCloseClick] 지정)
 *  - 뒤로 + 아바타 + 타이틀 ([showAvatar]/[title])
 *
 * 부모는 [androidx.compose.foundation.layout.statusBarsPadding] 만 적용.
 * 아바타↔타이틀(및 뒤로↔아바타) 간격 8dp.
 *
 * @param onCloseClick null 이 아니면 우측에 닫기 X 노출.
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyBackTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    showAvatar: Boolean = false,
    avatarUrl: String? = null,
    onCloseClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TopBarIcon(
                icon = R.drawable.ic_chevron_left,
                contentDescription = "뒤로가기",
                onClick = onBackClick,
            )
            if (showAvatar) {
                ModyAvatar(imageUrl = avatarUrl, size = 32.dp)
            }
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = ModyTheme.typography.b6,
                    color = ModyTheme.colors.gray10,
                )
            }
        }

        if (onCloseClick != null) {
            TopBarIcon(
                icon = ModyIcons.Plus1,
                contentDescription = "닫기",
                onClick = onCloseClick,
            )
        }
    }
}

/** 탑바 좌/우 아이콘 버튼. 24dp 영역, 글리프는 좌측 정렬(chevron 이 padding 24 지점에 오도록). */
@Composable
private fun TopBarIcon(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = ModyTheme.colors.gray10,
        )
    }
}
