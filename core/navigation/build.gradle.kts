plugins {
    alias(libs.plugins.mody.android.library)
    alias(libs.plugins.mody.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.core.navigation"
}

dependencies {
    // NavigationHelper 가 이벤트를 Flow 로, Route 가 @Serializable 직렬화기를 공개한다.
    // Route 만 정의하고 NavHost 는 만지지 않으므로 navigation-compose·json 은 필요 없다.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
}
