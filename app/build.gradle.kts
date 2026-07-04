import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// local.properties(gitignore)에서 카카오 네이티브 키 로드. 없으면 빈 문자열(컴파일은 됨).
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val kakaoNativeKeyDev: String = localProperties.getProperty("KAKAO_NATIVE_KEY_DEV", "")
val kakaoNativeKeyProd: String = localProperties.getProperty("KAKAO_NATIVE_KEY_PROD", "")

android {
    namespace = "com.makeus.mody"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.makeus.mody"
        minSdk = 26
        targetSdk = 36
        // CI(태그 push)에서 VERSION_CODE/VERSION_NAME 주입, 로컬은 fallback
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0-local"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_STORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: ""
            keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKeyDev\"")
            manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKeyDev
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKeyProd\"")
            manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKeyProd
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":presentation"))
    // DI 조립: Repository/DataSource 구현 바인딩(:core:data)을 Hilt 그래프에 포함
    implementation(project(":core:data"))
    implementation(project(":core:common-ui")) // CurrentActivityHolder 등록용
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kakao.user) // KakaoSdk.init


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
