package com.makeus.mody.core.designsystem.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * 메인 탭 화면 아래쪽에 떠 있는 하단 탭 바가 가리는 높이(시스템 내비게이션 인셋 포함).
 *
 * 하단 탭 바는 콘텐츠 위에 떠서 뒤를 흐리게 비춘다(글래스). 그래서 탭 콘텐츠는 화면 맨 아래까지 깔리고,
 * 스크롤 끝에 이만큼 여백을 둬야 마지막 항목이 바에 가려지지 않는다. 메인 화면이 값을 넣어 주고,
 * 탭 밖(상세 화면 등)에서는 0 이다.
 */
val LocalBottomBarInset = compositionLocalOf { 0.dp }
