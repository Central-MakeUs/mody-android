plugins {
    alias(libs.plugins.mody.android.library.compose)
    alias(libs.plugins.mody.android.hilt)
}

android {
    namespace = "com.makeus.mody.presentation"
}

/**
 * androidTest 변형에만 매니페스트 placeholder 를 채운다.
 *
 * MainActivity 의 App Links·카카오 스킴 intent-filter 가 `${inviteHost}` 와
 * `${KAKAO_NATIVE_KEY}` 를 쓰는데, 실제 값은 buildType 별로 갈려서 `:app` 에만 있다.
 * 라이브러리 본 변형은 치환하지 않은 채 넘겨 `:app` 이 채우므로 문제가 없지만,
 * androidTest 는 단독으로 APK 를 만들어 병합하는 탓에 채울 값이 없어 실패한다.
 *
 *   Attribute data@host requires a placeholder substitution
 *   but no value for <inviteHost> is provided.
 *
 * 지금은 `:presentation` 에 계측 테스트가 없어 이 태스크가 돌지 않아 드러나지 않는다.
 * 첫 계측 테스트를 짜는 순간 막힌다.
 *
 * defaultConfig 에 넣으면 안 된다 — 라이브러리 본 변형 매니페스트에 값이 구워져
 * `:app` 의 release 가 dev 호스트를 받는다. 테스트 변형에만 주입한다.
 */
androidComponents {
    onVariants { variant ->
        variant.androidTest?.manifestPlaceholders?.apply {
            // 실물과 겹치지 않는 값. 계측 테스트는 이 intent-filter 를 쓰지 않는다.
            put("inviteHost", "androidtest.invalid")
            put("KAKAO_NATIVE_KEY", "androidtest")
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:common-ui"))
    implementation(project(":core:domain"))
    // NavGraph 조립·하단 탭 화면
    implementation(project(":feature:auth:impl"))
    implementation(project(":feature:onboarding:impl"))
    implementation(project(":feature:group:impl"))
    implementation(project(":feature:feed:impl"))
    implementation(project(":feature:challenge:impl"))
    implementation(project(":feature:record:impl"))
    implementation(project(":feature:notification:impl"))
    implementation(project(":feature:mypage:impl"))
    // 시작 목적지 분기·딥링크(Route, NotificationLinkParser)
    implementation(project(":feature:auth:api"))
    implementation(project(":feature:onboarding:api"))
    implementation(project(":feature:group:api"))
    implementation(project(":feature:notification:api"))
    implementation(project(":feature:mypage:api"))

    // MainViewModel 등이 ViewModel 을 직접 상속한다.
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
}
