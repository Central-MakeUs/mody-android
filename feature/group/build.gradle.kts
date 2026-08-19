plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.makeus.mody.feature.group"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 카카오 콘솔 메시지 템플릿 id. 초대 카드의 문구·이미지·버튼 링크는 전부 콘솔에 있고
    // 앱은 이 id 와 치환 인자(code·groupName)만 넘긴다 — KakaoInviteSharer 주석 참고.
    buildTypes {
        debug {
            buildConfigField("long", "KAKAO_SHARE_TEMPLATE_ID", "135810L") // 모디 DEV
        }
        release {
            buildConfigField("long", "KAKAO_SHARE_TEMPLATE_ID", "135811L") // 모디 PROD
        }
    }
}

dependencies {
    implementation(project(":core:common-ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:domain"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kakao.share)
    // 그룹 생성 대기 다이얼로그의 캐릭터 로티(loading_group_create).
    // 공용 로딩(ModyLoadingIndicator)과 다른 전용 애니메이션이라 이 모듈에 둔다.
    implementation(libs.lottie.compose)
    // GroupCreatingOverlay 의 @Preview.
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}
