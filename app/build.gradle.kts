import java.util.Properties

plugins {
    alias(libs.plugins.mody.android.application)
    alias(libs.plugins.mody.android.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    // 앱 시작 경로 baseline profile 을 :baselineprofile 에서 받아 release 에 넣는다.
    alias(libs.plugins.baselineprofile)
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

    defaultConfig {
        applicationId = "com.makeus.mody"
        // CI(태그 push)에서 VERSION_CODE/VERSION_NAME 주입, 로컬 빌드는 아래 기본값.
        // 스토어에 올릴 AAB 를 로컬에서 빌드할 땐 이 기본값이 그대로 실리므로
        // 릴리스마다 versionCode 를 1 올리고 versionName 을 갱신할 것.
        versionCode = (System.getenv("VERSION_CODE") ?: "7").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }

    // 서명값도 env(CI) → local.properties(로컬) 순으로 조회. 둘 다 gitignore 라 키/비번 커밋 안 됨.
    fun signingProp(key: String, default: String = ""): String =
        System.getenv(key) ?: localProperties.getProperty(key, default)

    signingConfigs {
        /**
         * debug 를 레포에 커밋된 키스토어로 고정한다.
         *
         * 지정하지 않으면 AGP 가 `~/.android/debug.keystore` 를 쓰는데, CI 러너에는 그
         * 파일이 없어 매 빌드마다 랜덤 키로 새로 만든다. 그러면 서명이 빌드마다 달라져
         *   - 카카오 로그인이 안 된다(키 해시를 콘솔에 등록할 수가 없다)
         *   - 이전 빌드 위에 설치가 거부된다(INSTALL_FAILED_UPDATE_INCOMPATIBLE)
         *
         * 팀원과 CI 가 같은 키를 쓰므로 키 해시는 q9knJdxCQQrnHKBcHoFOu36Xbk4= 하나다.
         * 비밀번호는 안드로이드 debug 키스토어 기본값이라 비밀이 아니다. 릴리스 서명키와는
         * 완전히 별개다(그쪽은 local.properties/CI 시크릿).
         */
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
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
            // 개발 중 크래시가 대시보드를 덮지 않도록 수집 자체를 끈다(수집 여부는
            // ModyApplication 에서도 한 번 더 막는다). 매핑 업로드는 빌드만 느리게 해서 제외.
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKeyDev\"")
            manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKeyDev
            // 그룹 초대 App Links host. 이 호스트로 오는 https 링크만 앱이 가로챈다
            // (presentation manifest 의 intent-filter). 카카오 콘솔 템플릿의 웹 링크
            // 도메인과 어긋나면 링크가 앱을 안 열고 브라우저로 떨어진다.
            manifestPlaceholders["inviteHost"] = "dev-mody.store"
        }
        release {
            isMinifyEnabled = true
            // 난독화된 스택트레이스를 되돌리려면 매핑 업로드가 필수다.
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }
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
}

dependencies {
    implementation(project(":presentation"))
    // 설치 시 baseline profile 을 바로 적용(Play 클라우드 프로필이 오기 전·사이드로드 설치에도).
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    // DI 조립: Repository/DataSource 구현 바인딩(:core:data)을 Hilt 그래프에 포함
    implementation(project(":core:data"))
    implementation(project(":core:common-ui")) // CurrentActivityHolder 등록용
    implementation(project(":core:domain")) // PushTokenRepository, NotificationDeepLinkHolder
    implementation(project(":core:navigation")) // 알림 탭 딥링크 → NavigationHelper
    implementation(libs.kakao.user) // KakaoSdk.init
    implementation(libs.androidx.core.ktx) // NotificationCompat, getSystemService
    implementation(libs.kotlinx.coroutines.android) // FCM 토큰 등록 백그라운드 스코프
    // FCM 수신(푸시). messaging 만 있으면 됨(BOM 이 버전 정렬).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // 수집 on/off 스위치만 :app 에서 만진다. 실제 보고는 :core:data 의 ErrorReporter 구현.
    implementation(libs.firebase.crashlytics)


    testImplementation(libs.junit)
    // 아키텍처 규칙 검사. :app 이 모든 모듈을 보므로 여기서 전체 소스를 스캔한다.
    testImplementation(libs.konsist)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

/**
 * Compose 핵심 라이브러리(ui·foundation·animation) 버전이 한 줄로 맞는지 검사한다.
 *
 * 다른 라이브러리(예: lifecycle 2.10 → ui 1.9)가 BOM 보다 높은 버전을 끌어올리면 release 에는
 * ui 1.9 + foundation 1.7 처럼 섞인 조합이 들어간다. debug 는 ui-tooling(debugImplementation)이
 * 나머지까지 올려 우연히 맞기 때문에 개발 중엔 안 보이고 스토어 빌드에서만 터진다
 * (1.2.x 댓글 이모지 탭 먹통). release 안에서 셋이 같은지, debug 와 release 가 같은지 본다.
 */
val checkComposeVersions by tasks.registering {
    group = "verification"
    description = "Compose ui/foundation/animation 버전이 release 안에서, 그리고 debug 와 같은지 검사"
    val groups = setOf("androidx.compose.ui", "androidx.compose.foundation", "androidx.compose.animation")
    fun composeVersions(configuration: String) = configurations.named(configuration)
        .flatMap { it.incoming.resolutionResult.rootComponent }
        .map { root ->
            val seen = mutableSetOf<ResolvedComponentResult>()
            val found = sortedMapOf<String, String>()
            fun walk(component: ResolvedComponentResult) {
                if (!seen.add(component)) return
                val id = component.moduleVersion
                if (id != null && id.group in groups) found["${id.group}:${id.name}"] = id.version
                component.dependencies.forEach { dependency ->
                    when (dependency) {
                        is ResolvedDependencyResult -> walk(dependency.selected)
                        // 해석 실패한 간선을 건너뛰면 남은 모듈만 보고 "OK" 가 나온다 — 그대로 실패시킨다.
                        is UnresolvedDependencyResult -> throw GradleException(
                            "$configuration 의존성 해석 실패: ${dependency.attempted.displayName}",
                            dependency.failure,
                        )
                    }
                }
            }
            walk(root)
            found.toMap()
        }
    val release = composeVersions("releaseRuntimeClasspath")
    val debug = composeVersions("debugRuntimeClasspath")
    doLast {
        val releaseVersions = release.get()
        val debugVersions = debug.get()
        val problems = mutableListOf<String>()
        val distinct = releaseVersions.values.toSortedSet()
        if (distinct.size > 1) {
            problems += "release 안에서 버전이 섞임 $distinct:\n" +
                releaseVersions.entries.joinToString("\n") { "  ${it.key} = ${it.value}" }
        }
        releaseVersions.forEach { (module, version) ->
            val other = debugVersions[module]
            if (other != null && other != version) problems += "debug/release 불일치: $module debug=$other release=$version"
        }
        if (problems.isNotEmpty()) {
            throw GradleException(
                "Compose 버전 정렬 실패 — composeBom 을 끌어올려진 ui 버전에 맞추세요.\n" + problems.joinToString("\n"),
            )
        }
        logger.lifecycle("Compose 버전 정렬 OK: ${distinct.single()}")
    }
}
