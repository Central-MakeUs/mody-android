import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * 앱 시작 baseline profile 생성 + 시작 시간 측정(Macrobenchmark).
 *
 * - 생성: `./gradlew :app:generateReleaseBaselineProfile` → app/src/release/generated/baselineProfiles/
 * - 측정: `./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`
 *   (StartupBenchmarks — 프로필 없음 vs 프로필 적용 콜드 스타트 비교)
 *
 * 앱 모듈이 아니라 테스트 모듈이라 규약 플러그인(mody.android.*) 대신 직접 설정한다. SDK 값은
 * build-logic KotlinAndroid.kt 와 맞춘다. minSdk 28 은 Macrobenchmark 최소 요구.
 */
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.makeus.mody.baselineprofile"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    targetProjectPath = ":app"
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

baselineProfile {
    // 연결된 기기(에뮬레이터 포함)에서 생성·측정한다.
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
