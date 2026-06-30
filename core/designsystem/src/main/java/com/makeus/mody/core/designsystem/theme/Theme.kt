package com.makeus.mody.core.designsystem.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle

private val ModyColorScheme = lightColorScheme(
    primary = Primary100,
    secondary = Secondary100,
    background = White,
    surface = White,
    onPrimary = Black,
    onSecondary = Black,
    onBackground = Gray10,
    onSurface = Gray10,
)

@Composable
fun ModyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ModyColorScheme

    val colors = remember { ModyColors() }
    val typography = remember { ModyTypography() }

    CompositionLocalProvider(
        LocalModyColors provides colors,
        LocalModyTypography provides typography,
        // 스타일 미지정 Text() 기본 폰트도 Pretendard 로
        LocalTextStyle provides TextStyle(fontFamily = PretendardFontFamily),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PretendardMaterialTypography,
            content = content,
        )
    }
}
