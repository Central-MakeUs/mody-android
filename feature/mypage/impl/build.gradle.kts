plugins {
    alias(libs.plugins.mody.android.feature)
}

android {
    namespace = "com.makeus.mody.feature.mypage"
}

dependencies {
    // 다른 feature 로 이동할 목적지(Route). :impl 은 서로 볼 수 없어 :api 만 선언한다.
    implementation(project(":feature:auth:api"))
    implementation(project(":feature:group:api"))
    implementation(project(":feature:notification:api"))

    implementation(libs.androidx.health.connect)
}
