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
// CI/빌드서버(local.properties 없음) 대비 env var fallback
val kakaoNativeKeyDev: String =
    System.getenv("KAKAO_NATIVE_KEY_DEV") ?: localProperties.getProperty("KAKAO_NATIVE_KEY_DEV", "")
val kakaoNativeKeyProd: String =
    System.getenv("KAKAO_NATIVE_KEY_PROD") ?: localProperties.getProperty("KAKAO_NATIVE_KEY_PROD", "")

android {
    namespace = "com.makeus.mody"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.makeus.mody"
        minSdk = 26
        targetSdk = 36
        // CI(태그 push)에서 VERSION_CODE/VERSION_NAME 주입, 로컬 빌드는 아래 기본값.
        // 스토어에 올릴 AAB 를 로컬에서 빌드할 땐 이 기본값이 그대로 실리므로
        // 릴리스마다 versionCode 를 1 올리고 versionName 을 갱신할 것.
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }

    // 서명값도 env(CI) → local.properties(로컬) 순으로 조회. 둘 다 gitignore 라 키/비번 커밋 안 됨.
    fun signingProp(key: String, default: String = ""): String =
        System.getenv(key) ?: localProperties.getProperty(key, default)

    signingConfigs {
        create("release") {
            storeFile = file(signingProp("SIGNING_STORE_FILE", "keystore.jks"))
            storePassword = signingProp("SIGNING_STORE_PASSWORD")
            keyAlias = signingProp("SIGNING_KEY_ALIAS")
            keyPassword = signingProp("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKeyDev\"")
            manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKeyDev
            // 그룹 초대 App Links host. feature:group INVITE_BASE_URL 과 도메인 일치 필수.
            manifestPlaceholders["inviteHost"] = "dev-mody.store"
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKeyProd\"")
            manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKeyProd
            manifestPlaceholders["inviteHost"] = "prod-mody.shop"
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
    implementation(project(":core:domain")) // PushTokenRepository, NotificationDeepLinkHolder
    implementation(project(":core:navigation")) // 알림 탭 딥링크 → NavigationHelper
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kakao.user) // KakaoSdk.init
    implementation(libs.androidx.core.ktx) // NotificationCompat, getSystemService
    implementation(libs.kotlinx.coroutines.android) // FCM 토큰 등록 백그라운드 스코프
    // FCM 수신(푸시). messaging 만 있으면 됨(BOM 이 버전 정렬).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
