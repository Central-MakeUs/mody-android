package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 로딩 인디케이터. API 호출을 기다리는 모든 화면에서 동일하게 사용.
 * 색상은 메인 컬러(primary100) 기본.
 */
@Composable
fun ModyLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = ModyTheme.colors.primary100,
) {
    CircularProgressIndicator(modifier = modifier, color = color)
}

/**
 * 화면 전체를 채우고 중앙에 로딩 인디케이터를 띄우는 래퍼.
 * 최초 로드 대기(값 준비 전) 시 콘텐츠 대신 표시.
 */
@Composable
fun ModyLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ModyLoadingIndicator()
    }
}
