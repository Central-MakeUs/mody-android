plugins {
    alias(libs.plugins.mody.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.feature.challenge"
}

dependencies {
    implementation(project(":core:camera"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.health.connect)

    testImplementation(libs.junit)
}
