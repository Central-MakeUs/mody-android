package com.makeus.mody.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 스토어 심사용 히든 로그인 다이얼로그.
 * 로그인 화면에서 로고를 연타하면 노출되고, 비밀번호가 맞으면 심사용 계정으로 로그인한다.
 */
@Composable
internal fun ReviewLoginDialog(
    password: String,
    isPasswordError: Boolean,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ModyTheme.colors.white)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "심사용 로그인",
                style = ModyTheme.typography.b2,
                color = ModyTheme.colors.gray10,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModyTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "비밀번호를 입력해주세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.gray01)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            if (isPasswordError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "비밀번호가 일치하지 않아요",
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModyButton(
                    text = "취소",
                    onClick = onDismiss,
                    variant = ModyButtonVariant.Gray,
                    modifier = Modifier.weight(1f),
                )
                ModyButton(
                    text = "확인",
                    onClick = onSubmit,
                    variant = ModyButtonVariant.Primary,
                    enabled = password.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
