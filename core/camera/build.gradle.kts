plugins {
    alias(libs.plugins.mody.android.library.compose)
}

android {
    namespace = "com.makeus.mody.core.camera"
}

dependencies {
    implementation(project(":core:designsystem"))
    // 크롭 결과를 도메인 모델(CropRegion)로 넘긴다. ModyCameraOverlay 의 공개 시그니처에
    // CropRegion 이 드러나므로 api — implementation 이면 호출부가 타입을 볼 수 없다.
    api(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    testImplementation(libs.junit)
}
