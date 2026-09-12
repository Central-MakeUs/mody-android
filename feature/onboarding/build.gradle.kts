plugins {
    alias(libs.plugins.mody.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.feature.onboarding"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.health.connect)
}
