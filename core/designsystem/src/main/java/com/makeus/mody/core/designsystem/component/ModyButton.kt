package com.makeus.mody.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

enum class ModyButtonVariant { Gray, Primary, Dark, Kakao, Google }

@Composable
fun ModyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ModyButtonVariant = ModyButtonVariant.Primary,
    enabled: Boolean = true,
    @DrawableRes leadingIcon: Int? = null,
    leadingIconSize: Dp = 20.dp,
    leadingIconSpacing: Dp = 8.dp,
) {
    val containerColor = when (variant) {
        ModyButtonVariant.Gray -> ModyTheme.colors.gray02
        ModyButtonVariant.Primary -> ModyTheme.colors.primary100
        ModyButtonVariant.Dark -> ModyTheme.colors.gray10
        ModyButtonVariant.Kakao -> ModyTheme.colors.kakaoYellow
        ModyButtonVariant.Google -> ModyTheme.colors.white
    }
    val contentColor = when (variant) {
        ModyButtonVariant.Gray -> ModyTheme.colors.gray10
        ModyButtonVariant.Primary -> ModyTheme.colors.gray10
        ModyButtonVariant.Dark -> ModyTheme.colors.white
        ModyButtonVariant.Kakao -> ModyTheme.colors.gray10
        ModyButtonVariant.Google -> ModyTheme.colors.gray10
    }
    val border = when (variant) {
        // disabled 시 외곽선도 비활성 색으로 (fill/text 처리와 일관)
        ModyButtonVariant.Google -> BorderStroke(
            1.dp,
            if (enabled) ModyTheme.colors.gray03 else ModyTheme.colors.gray02,
        )
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = ModyTheme.colors.gray02,
            disabledContentColor = ModyTheme.colors.gray05,
        ),
        contentPadding = PaddingValues(horizontal = 28.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(leadingIconSize),
                )
                Spacer(modifier = Modifier.width(leadingIconSpacing))
            }
            Text(
                text = text,
                style = ModyTheme.typography.b6,
            )
        }
    }
}
