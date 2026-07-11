package com.makeus.mody.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
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


// Figma 텍스트 메트릭과 1:1 매칭.
// includeFontPadding=false → Compose 레거시 상/하 여분 패딩 제거(Figma엔 없음).
// LineHeightStyle(trim=None) → Figma leading-trim:NONE 대로 line-height leading 유지,
//   alignment=Center로 위/아래 균등 분산. → 박스 높이 = 정확히 line-height.
// → Figma의 dp 간격이 실기기에서 그대로 맞는다.
private fun modyTextStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
): TextStyle = TextStyle(
    fontFamily = PretendardFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

data class ModyTypography(
    val h0: TextStyle = modyTextStyle(FontWeight.Bold, 36.sp, 50.4.sp, 0.36.sp), // spacing 1% of 36sp
    val h1: TextStyle = modyTextStyle(FontWeight.Bold, 28.sp, 39.2.sp),
    val h2: TextStyle = modyTextStyle(FontWeight.Bold, 24.sp, 33.6.sp),
    val h3: TextStyle = modyTextStyle(FontWeight.Bold, 20.sp, 28.sp),
    val b1: TextStyle = modyTextStyle(FontWeight.SemiBold, 22.sp, 30.8.sp),
    val b2: TextStyle = modyTextStyle(FontWeight.SemiBold, 20.sp, 28.sp),
    val b3: TextStyle = modyTextStyle(FontWeight.SemiBold, 18.sp, 25.2.sp),
    val b4: TextStyle = modyTextStyle(FontWeight.Medium, 18.sp, 25.2.sp),
    val b5: TextStyle = modyTextStyle(FontWeight.Bold, 16.sp, 22.4.sp),
    val b6: TextStyle = modyTextStyle(FontWeight.SemiBold, 16.sp, 22.4.sp),
    val b7: TextStyle = modyTextStyle(FontWeight.Medium, 16.sp, 22.4.sp),
    val c1: TextStyle = modyTextStyle(FontWeight.Medium, 14.sp, 19.6.sp),
    val c2: TextStyle = modyTextStyle(FontWeight.SemiBold, 12.sp, 16.8.sp),
    val c3: TextStyle = modyTextStyle(FontWeight.Medium, 12.sp, 16.8.sp),
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
