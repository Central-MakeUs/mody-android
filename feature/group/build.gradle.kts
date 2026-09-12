plugins {
    alias(libs.plugins.mody.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.feature.group"
    buildFeatures { buildConfig = true }

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
    implementation(libs.androidx.activity.compose)
    implementation(libs.kakao.share)
    implementation(libs.lottie.compose)
}
