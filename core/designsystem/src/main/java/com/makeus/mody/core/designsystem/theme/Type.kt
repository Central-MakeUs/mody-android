package com.makeus.mody.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makeus.mody.core.designsystem.R

// Pretendard 폰트 파일을 core/designsystem/src/main/res/font/ 에 추가 필요
// pretendard_bold.ttf, pretendard_semibold.ttf, pretendard_medium.ttf
val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_medium, FontWeight.Medium),
)


data class ModyTypography(
    val h1: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 39.2.sp,
        letterSpacing = 0.sp,
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 33.6.sp,
        letterSpacing = 0.sp,
    ),
    val h3: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    val b1: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    val b2: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 25.2.sp,
        letterSpacing = 0.sp,
    ),
    val b3: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.2.sp,
        letterSpacing = 0.sp,
    ),
    val b4: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.4.sp,
        letterSpacing = 0.sp,
    ),
    val b5: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.4.sp,
        letterSpacing = 0.sp,
    ),
    val c1: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
        letterSpacing = 0.sp,
    ),
)

val LocalModyTypography = staticCompositionLocalOf { ModyTypography() }
