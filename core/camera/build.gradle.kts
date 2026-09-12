plugins {
    alias(libs.plugins.mody.android.library.compose)
}

android {
    namespace = "com.makeus.mody.core.camera"
}

dependencies {
    implementation(project(":core:designsystem"))
    // 크롭 결과를 CropRegion 으로 넘긴다. ModyCameraOverlay 의 공개 시그니처에 드러나므로
    // api — implementation 이면 호출부가 타입을 볼 수 없다.
    // Repository·UseCase 는 쓰지 않으므로 :core:domain 이 아니라 :core:model 만 본다.
    api(project(":core:model"))

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
