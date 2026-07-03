package com.makeus.mody.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

@Composable
fun ModyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    textStyle: TextStyle = ModyTheme.typography.b4.copy(color = ModyTheme.colors.gray10),
    placeholderStyle: TextStyle = ModyTheme.typography.b4,
    placeholderColor: Color = ModyTheme.colors.gray04,
    @DrawableRes leadingIcon: Int? = null,
    leadingIconSize: Dp = 20.dp,
    leadingIconSpacing: Dp = 8.dp,
    @DrawableRes trailingIcon: Int? = null,
    trailingIconSize: Dp = 20.dp,
    trailingIconSpacing: Dp = 8.dp,
    onTrailingIconClick: (() -> Unit)? = null,
    trailingIconContentDescription: String? = null,
    @DrawableRes alertIcon: Int? = null,
    alertIconSize: Dp = 20.dp,
    alertIconSpacing: Dp = 8.dp,
    cursorBrush: Brush = SolidColor(ModyTheme.colors.primary100),
    enabled: Boolean = true,
    maxLength: Int? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (maxLength == null || newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        singleLine = singleLine,
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
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

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = placeholderStyle,
                            color = placeholderColor,
                        )
                    }
                    innerTextField()
                }

                if (alertIcon != null) {
                    Spacer(modifier = Modifier.width(alertIconSpacing))
                    Icon(
                        painter = painterResource(alertIcon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(alertIconSize),
                    )
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(trailingIconSpacing))
                    // enabled=false면 클릭 비활성. (터치영역은 시각 크기와 동일 — 필드 높이 유지 위해)
                    val interactive = onTrailingIconClick != null && enabled
                    Icon(
                        painter = painterResource(trailingIcon),
                        contentDescription = trailingIconContentDescription,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(trailingIconSize)
                            .then(
                                if (interactive) Modifier.clickable(onClick = onTrailingIconClick)
                                else Modifier
                            ),
                    )
                }
            }
        },
    )
}
