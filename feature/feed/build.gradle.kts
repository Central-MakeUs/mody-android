plugins {
    alias(libs.plugins.mody.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.feature.feed"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}
