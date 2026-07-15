package com.makeus.mody.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary
// Figma "Main0" — Main(primary100)보다 진한 강조 옐로 (예: 사진 업로드 문구)
val Primary0 = Color(0xFFFBD406)
val Primary100 = Color(0xFFFFE24A)
val Primary200 = Color(0xFFFFEE92)
val Primary300 = Color(0xFFFFF6C9)
val Primary400 = Color(0xFFFFFCED)

// Secondary
val Secondary100 = Color(0xFF6E7CFF)
val Secondary200 = Color(0xFFA8B0FF)
val Secondary300 = Color(0xFFD3D8FF)
val Secondary400 = Color(0xFFF0F2FF)

// Gray Scale
val Gray10 = Color(0xFF111111)
val Gray09 = Color(0xFF2F2F2F)
val Gray08 = Color(0xFF4C4C4C)
val Gray07 = Color(0xFF6A6A6A)
val Gray06 = Color(0xFF878787)
val Gray05 = Color(0xFFA5A5A5)
val Gray04 = Color(0xFFB7B7B7)
val Gray03 = Color(0xFFC9C9C9)
val Gray02 = Color(0xFFE4E4E4)
val Gray01 = Color(0xFFF6F6F6)

// System
val Black = Color(0xFF000000)
val Error = Color(0xFFFC2C30)
val White = Color(0xFFFFFFFF)

// Brand
val KakaoYellow = Color(0xFFFEE500)

data class ModyColors(
    val primary0: Color = Primary0,
    val primary100: Color = Primary100,
    val primary200: Color = Primary200,
    val primary300: Color = Primary300,
    val primary400: Color = Primary400,
    val secondary100: Color = Secondary100,
    val secondary200: Color = Secondary200,
    val secondary300: Color = Secondary300,
    val secondary400: Color = Secondary400,
    val gray10: Color = Gray10,
    val gray09: Color = Gray09,
    val gray08: Color = Gray08,
    val gray07: Color = Gray07,
    val gray06: Color = Gray06,
    val gray05: Color = Gray05,
    val gray04: Color = Gray04,
    val gray03: Color = Gray03,
    val gray02: Color = Gray02,
    val gray01: Color = Gray01,
    val black: Color = Black,
    val white: Color = White,
    val kakaoYellow: Color = KakaoYellow,
    val error: Color = Error,
)

val LocalModyColors = staticCompositionLocalOf { ModyColors() }
