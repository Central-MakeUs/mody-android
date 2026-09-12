plugins {
    alias(libs.plugins.mody.android.library)
}

android {
    namespace = "com.makeus.mody.core.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
