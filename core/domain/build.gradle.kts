plugins {
    alias(libs.plugins.mody.jvm.library)
}

dependencies {
    // Repository·UseCase 시그니처에 모델이 그대로 드러나므로 api.
    api(project(":core:model"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
