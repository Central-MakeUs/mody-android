package com.makeus.mody.feature.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyTopBarIcon
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme

// 상태바 아래 타이틀 시작 지점(온보딩 진행바+간격과 동일). 상단 영역 고정 높이.
private val TitleTopOffset = 72.dp

/**
 * 그룹 플로우 공통 골격.
 * 온보딩(OnboardingScaffold)과 **타이틀/서브타이틀 위치를 동일**하게 맞춘다.
 * 차이는 상단 진행바가 없다는 것뿐 — 진행바 자리는 뒤로가기 버튼(있을 때) 또는 빈 공간이 대체하며,
 * 타이틀은 상태바로부터 동일한 72dp 지점에서 시작한다.
 *
 * @param onBackClick null 이면 뒤로가기 버튼 미표시(엔트리 화면).
 * @param imeAware    true 면 키보드 인셋까지 패딩(입력 화면). false 면 네비바만.
 * @param titleContentSpacing null이면 자동: 서브타이틀 있으면 60dp, 없으면 48dp (온보딩과 동일).
 */
@Composable
fun GroupScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: AnnotatedString? = null,
    onBackClick: (() -> Unit)? = null,
    imeAware: Boolean = true,
    titleContentSpacing: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets =
        if (imeAware) WindowInsets.ime.union(WindowInsets.navigationBars)
        else WindowInsets.navigationBars

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .windowInsetsPadding(insets)
            // 좌우 24 (Figma). 위아래(타이틀/서브 위치)는 온보딩과 동일.
            .padding(horizontal = 24.dp),
    ) {
        // 온보딩 진행바 자리를 고정 높이 상단 영역으로 대체 → 타이틀이 항상 동일 지점에서 시작.
        // (뒤로가기 버튼 크기 변경과 무관하게 위치 유지)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TitleTopOffset),
            contentAlignment = Alignment.TopStart,
        ) {
            if (onBackClick != null) {
                // ModyBackTopBar(기록/알림/상세)와 백키 위치 통일: chevron 좌 24dp(=Column padding), 상 12dp.
                // 공용 ModyTopBarIcon 재사용(탑바 아이콘 동작 일관).
                ModyTopBarIcon(
                    icon = ModyIcons.ChevronLeft,
                    contentDescription = "뒤로가기",
                    onClick = onBackClick,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        Text(
            text = title,
            style = ModyTheme.typography.h2,
            color = ModyTheme.colors.gray10,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray06,
            )
        }

        val contentSpacing = titleContentSpacing ?: if (subtitle != null) 60.dp else 48.dp
        Spacer(modifier = Modifier.height(contentSpacing))

        content()
    }
}
