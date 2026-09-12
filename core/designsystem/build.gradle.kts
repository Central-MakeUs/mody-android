plugins {
    alias(libs.plugins.mody.android.library.compose)
}

android {
    namespace = "com.makeus.mody.core.designsystem"
}

dependencies {
    implementation(libs.coil.compose)
    // 공용 로딩 인디케이터(ModyLoadingIndicator)의 로티 재생용.
    implementation(libs.lottie.compose)
}
