plugins {
    alias(libs.plugins.mody.android.library)
    alias(libs.plugins.mody.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.core.navigation"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
}
