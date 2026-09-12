import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.makeus.mody.buildlogic"

// 본 빌드가 Java 11 이므로 규약 플러그인도 11 로 맞춘다.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // 플러그인 코드가 AGP/Kotlin 의 타입(LibraryExtension 등)을 참조하기 위한 것.
    // 실제 플러그인 적용은 소비 빌드의 클래스패스(루트 build.gradle.kts 의 apply false)에서 온다.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "mody.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "mody.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "mody.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "mody.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "mody.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
