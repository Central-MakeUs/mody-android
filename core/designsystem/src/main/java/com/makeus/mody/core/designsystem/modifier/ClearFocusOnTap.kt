package com.makeus.mody.core.designsystem.modifier

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * 빈 영역(텍스트필드·클릭 요소가 아닌 곳)을 탭하면 포커스 해제 + 키보드 닫음.
 * 텍스트필드가 있는 화면의 root Modifier에 붙여 공통 동작으로 사용.
 *
 * 자식의 clickable/텍스트필드가 탭을 먼저 소비하므로, 빈 영역 탭에서만 동작한다.
 */
fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    pointerInput(Unit) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
}
