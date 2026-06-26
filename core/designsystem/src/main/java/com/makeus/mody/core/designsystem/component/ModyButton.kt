package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

private val KakaoYellow = Color(0xFFFEE500)

enum class ModyButtonVariant { Gray, Primary, Dark, Kakao }

@Composable
fun ModyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ModyButtonVariant = ModyButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val containerColor = when (variant) {
        ModyButtonVariant.Gray -> ModyTheme.colors.gray02
        ModyButtonVariant.Primary -> ModyTheme.colors.primary100
        ModyButtonVariant.Dark -> ModyTheme.colors.gray10
        ModyButtonVariant.Kakao -> KakaoYellow
    }
    val contentColor = when (variant) {
        ModyButtonVariant.Gray -> ModyTheme.colors.gray10
        ModyButtonVariant.Primary -> ModyTheme.colors.gray10
        ModyButtonVariant.Dark -> ModyTheme.colors.white
        ModyButtonVariant.Kakao -> ModyTheme.colors.gray10
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = ModyTheme.colors.gray02,
            disabledContentColor = ModyTheme.colors.gray05,
        ),
        contentPadding = PaddingValues(horizontal = 28.dp),
    ) {
        Text(
            text = text,
            style = ModyTheme.typography.b4,
        )
    }
}
