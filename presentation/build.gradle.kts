plugins {
    alias(libs.plugins.mody.android.library.compose)
    alias(libs.plugins.mody.android.hilt)
}

android {
    namespace = "com.makeus.mody.presentation"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:common-ui"))
    implementation(project(":core:domain"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:group"))
    implementation(project(":feature:feed"))
    implementation(project(":feature:challenge"))
    implementation(project(":feature:record"))
    implementation(project(":feature:notification"))
    implementation(project(":feature:mypage"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
}
