plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.makeus.mody.core.data"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    // Firebase Remote Config (기능 플래그: 챌린지 탭 노출 제어)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
}
