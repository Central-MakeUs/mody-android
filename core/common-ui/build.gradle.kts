plugins {
    alias(libs.plugins.mody.android.library)
    alias(libs.plugins.mody.android.hilt)
}

android {
    namespace = "com.makeus.mody.core.commonui"
}

dependencies {
    // 건강 데이터 연동 진입 헬퍼가 HealthAvailability 로 분기한다.
    // 값 타입만 쓰므로 :core:domain 이 아니라 :core:model 만 본다.
    // openHealthConnect(context, availability) 의 공개 시그니처에 드러나므로 api.
    api(project(":core:model"))

    // BaseViewModel 이 ViewModel 을 상속하고 state 를 StateFlow 로 내보낸다. 둘 다 공개
    // 시그니처라 api — 소비자가 lifecycle·coroutines 를 각자 선언한 덕에 컴파일되던 상태였다.
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Health Connect 설정 화면 액션 상수만 사용.
    implementation(libs.androidx.health.connect)
}
