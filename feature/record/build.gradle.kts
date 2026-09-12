plugins {
    alias(libs.plugins.mody.android.feature)
}

android {
    namespace = "com.makeus.mody.feature.record"
}

dependencies {
    implementation(project(":core:camera"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}
