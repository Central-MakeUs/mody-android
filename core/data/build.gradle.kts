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
    // 세션 토큰 암호화 저장 (EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)

    // 이미지 스트리밍 업로드용 RequestBody 구성 (PresignedUploader 에 전달)
    implementation(libs.okhttp)

    // Health Connect (걸음 수 읽기)
    implementation(libs.androidx.health.connect)

    // Firebase Remote Config (기능 플래그: 챌린지 탭 노출 제어)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
}
