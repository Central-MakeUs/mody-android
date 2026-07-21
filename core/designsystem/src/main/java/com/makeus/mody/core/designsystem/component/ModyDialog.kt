package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 공용 알림/확인 다이얼로그. 제목 + (선택)본문 + 버튼 1~2개.
 *
 * 두 가지 형태를 한 컴포넌트로:
 *  - **알림/에러형**: [dismissText] = null → 확인 버튼 하나만. 에러 메시지 노출·재시도 유도 등.
 *  - **확인형**: [dismissText] 지정 → 취소 + 확정 두 버튼(예: 탈퇴 확인).
 *
 * 파괴적 확정(계정 삭제 등)은 [confirmContainerColor]/[confirmContentColor] 로 강조색을 넘긴다.
 *
 * @param onDismissRequest 스크림 탭·백키 등 시스템 닫기. 취소 버튼도 기본적으로 이걸 호출.
 * @param onConfirm 확정 버튼 클릭. 호출 측에서 다이얼로그를 닫아야 한다(자동 닫힘 아님).
 */
// 다른 모듈(:feature:*)에서만 쓰는 public API → 모듈 내부 미사용 오탐 억제.
@Suppress("unused")
@Composable
fun ModyDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = onDismissRequest,
    confirmContainerColor: Color = ModyTheme.colors.primary100,
    confirmContentColor: Color = ModyTheme.colors.gray10,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ModyTheme.colors.white)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = ModyTheme.typography.b7,
                    color = ModyTheme.colors.gray06,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dismissText != null) {
                    DialogButton(
                        text = dismissText,
                        containerColor = ModyTheme.colors.gray01,
                        contentColor = ModyTheme.colors.gray05,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                }
                DialogButton(
                    text = confirmText,
                    containerColor = confirmContainerColor,
                    contentColor = confirmContentColor,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = ModyTheme.typography.b5, color = contentColor)
    }
}
