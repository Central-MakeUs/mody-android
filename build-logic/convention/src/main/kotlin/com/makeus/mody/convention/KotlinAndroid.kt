package com.makeus.mody.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** 모든 안드로이드 모듈이 공유하는 SDK·자바 버전. 여기 한 곳에서만 올린다. */
internal const val COMPILE_SDK = 36
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 36
internal val JAVA_VERSION = JavaVersion.VERSION_11

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = COMPILE_SDK

        defaultConfig {
            minSdk = MIN_SDK
        }

        compileOptions {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
    }

    // kotlinOptions DSL 은 AGP 확장마다 타입이 갈리므로 태스크 쪽에서 직접 맞춘다.
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JAVA_VERSION.toString()
        targetCompatibility = JAVA_VERSION.toString()
    }
}

/**
 * Compose 를 쓰는 모듈의 공통 설정.
 *
 * BOM·ui·material3·미리보기 도구까지 여기서 붙인다. 모듈마다 따로 적으면 한쪽만
 * 빠진 채 Preview 가 안 보이는 식으로 갈린다.
 */
internal fun Project.configureCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        add("implementation", platform(libs.library("androidx-compose-bom")))
        add("implementation", libs.library("androidx-ui"))
        add("implementation", libs.library("androidx-ui-graphics"))
        add("implementation", libs.library("androidx-ui-tooling-preview"))
        add("implementation", libs.library("androidx-material3"))
        // collectAsStateWithLifecycle. 9개 모듈이 쓰면서 아무도 선언하지 않아
        // hilt-navigation-compose 의 전이 의존에 얹혀 있었다 — 그쪽이 끊으면 한꺼번에 깨진다.
        add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
        add("debugImplementation", libs.library("androidx-ui-tooling"))
    }
}
