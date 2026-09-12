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
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}
