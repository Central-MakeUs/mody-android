package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 모든 일반 화면의 공통 루트. 시스템 바 inset(상태바/내비게이션 바)을 한 곳에서 강제해
 * 화면마다 statusBarsPadding 을 빠뜨려 탑바가 상태바 밑으로 파고드는 실수를 막는다.
 *
 * 구조: `Column(fillMaxSize + background + statusBarsPadding + navigationBarsPadding)` → topBar → content.
 * 탑바는 슬롯이라 [ModyBackTopBar]/[ModyLogoTopBar] 등 무엇이든 넣을 수 있다.
 *
 * @param applyImePadding 입력 필드가 있어 키보드가 뜨는 화면(예: 프로필 편집)만 true.
 *  토글/목록만 있는 화면은 false(기본) — 불필요한 리컴포지션/패딩 방지.
 * @param topBar 상단 고정 탑바 슬롯. 없으면 비워둔다.
 * @param content 탑바 아래 본문. 스크롤(LazyColumn/verticalScroll)은 본문에서 각자 처리.
 */
@Composable
fun ModyScreenScaffold(
    modifier: Modifier = Modifier,
    background: Color = ModyTheme.colors.white,
    applyImePadding: Boolean = false,
    topBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .then(if (applyImePadding) Modifier.imePadding() else Modifier),
    ) {
        topBar()
        content()
    }
}
