plugins {
    alias(libs.plugins.mody.android.library)
    alias(libs.plugins.mody.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.makeus.mody.core.network"
    buildFeatures { buildConfig = true }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://dev-mody.store/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://prod-mody.shop/\"")
        }
    }
}

dependencies {
    // domain 인터페이스를 구현하고, Retrofit API·OkHttpClient 를 :core:data 에 내준다.
    // 공개 시그니처에 드러나므로 api.
    api(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    api(libs.okhttp)
    // 공개 DTO 의 @Serializable 직렬화기가 core 타입을 드러낸다. json 은 내부 구현이라 그대로 둔다.
    api(libs.kotlinx.serialization.core)
    implementation(libs.okhttp.logging)
}
