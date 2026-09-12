plugins {
    alias(libs.plugins.mody.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.feature.auth"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.kakao.user)
    // 구글 access token 획득(Identity Authorization API) + Activity Result 사용
    implementation(libs.play.services.auth)
}
