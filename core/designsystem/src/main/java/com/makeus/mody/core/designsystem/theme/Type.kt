package com.makeus.mody.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makeus.mody.core.designsystem.R

// Pretendard 폰트: core/designsystem/src/main/res/font/
// pretendard_regular.otf, pretendard_medium.otf, pretendard_semibold.otf, pretendard_bold.otf
val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)


data class ModyTypography(
    val h0: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 50.4.sp,
        letterSpacing = 0.36.sp, // 1% of 36sp
    ),
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
        fontSize = 22.sp,
        lineHeight = 30.8.sp,
        letterSpacing = 0.sp,
    ),
    val b2: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    val b3: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 25.2.sp,
        letterSpacing = 0.sp,
    ),
    val b4: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.2.sp,
        letterSpacing = 0.sp,
    ),
    val b5: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.4.sp,
        letterSpacing = 0.sp,
    ),
    val b6: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.4.sp,
        letterSpacing = 0.sp,
    ),
    val b7: TextStyle = TextStyle(
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
    val c2: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.sp,
    ),
    val c3: TextStyle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.sp,
    ),
)

val LocalModyTypography = staticCompositionLocalOf { ModyTypography() }

/**
 * Material3 컴포넌트(Button, TopAppBar 등) 기본 폰트를 Pretendard 로 강제.
 * 15개 텍스트 스타일 전부 fontFamily 만 교체.
 */
val PretendardMaterialTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = PretendardFontFamily),
        displayMedium = displayMedium.copy(fontFamily = PretendardFontFamily),
        displaySmall = displaySmall.copy(fontFamily = PretendardFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = PretendardFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = PretendardFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = PretendardFontFamily),
        titleLarge = titleLarge.copy(fontFamily = PretendardFontFamily),
        titleMedium = titleMedium.copy(fontFamily = PretendardFontFamily),
        titleSmall = titleSmall.copy(fontFamily = PretendardFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = PretendardFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = PretendardFontFamily),
        bodySmall = bodySmall.copy(fontFamily = PretendardFontFamily),
        labelLarge = labelLarge.copy(fontFamily = PretendardFontFamily),
        labelMedium = labelMedium.copy(fontFamily = PretendardFontFamily),
        labelSmall = labelSmall.copy(fontFamily = PretendardFontFamily),
    )
}
