plugins {
    alias(libs.plugins.mody.android.feature)
}

android {
    namespace = "com.makeus.mody.feature.mypage"
}

dependencies {
    implementation(libs.androidx.health.connect)
    implementation(libs.coil.compose)
}
