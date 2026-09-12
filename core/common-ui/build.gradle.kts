plugins {
    alias(libs.plugins.mody.android.library)
    alias(libs.plugins.mody.android.hilt)
}

android {
    namespace = "com.makeus.mody.core.commonui"
}

dependencies {
    // 건강 데이터 연동 진입 헬퍼가 HealthAvailability 로 분기한다.
    implementation(project(":core:domain"))

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Health Connect 설정 화면 액션 상수만 사용.
    implementation(libs.androidx.health.connect)
}
