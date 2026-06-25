package com.makeus.mody.core.designsystem.theme

import androidx.compose.runtime.Composable

object ModyTheme {
    val colors: ModyColors
        @Composable get() = LocalModyColors.current

    val typography: ModyTypography
        @Composable get() = LocalModyTypography.current
}
