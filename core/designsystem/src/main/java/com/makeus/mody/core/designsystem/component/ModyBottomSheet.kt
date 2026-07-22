package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 바텀시트. material3 [ModalBottomSheet]를 감싸 프로젝트 표준을 한 곳에 고정한다.
 *  - 상단 라운드(36dp), 컨테이너 흰색
 *  - 하단 safe area는 material 기본(`contentWindowInsets = safeDrawing.only(Bottom)`)이
 *    navigationBars/ime를 자동 처리하므로 별도 패딩 불필요. content는 좌우/상하 여백만 지정.
 *
 * @param skipPartiallyExpanded true면 중간(반만 펼침) 단계 없이 콘텐츠 높이만큼 바로 펼침.
 *   내용이 화면 절반보다 길어(예: 입력폼+휠+버튼) 하단 버튼이 접힘 아래로 숨는 걸 방지.
 *
 * 새 바텀시트는 material3 [ModalBottomSheet] 대신 항상 이 컴포넌트를 쓴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModyBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        containerColor = ModyTheme.colors.white,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}
