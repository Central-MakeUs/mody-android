plugins {
    alias(libs.plugins.mody.android.feature.api)
}

android {
    namespace = "com.makeus.mody.feature.notification.api"
}

dependencies {
    // 알림 link → 기록 입력 화면 매핑(NotificationLinkParser)
    implementation(project(":feature:record:api"))
}
