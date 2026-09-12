plugins {
    alias(libs.plugins.mody.jvm.library)
}

// 의존성 없음 — 모든 계층이 공유하는 값 타입만 둔다.
dependencies {
    testImplementation(libs.junit)
}
