package com.makeus.mody.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * 규약 플러그인에서 버전 카탈로그를 읽는 통로.
 *
 * `libs.xxx` 타입 접근자는 빌드 스크립트에서만 생성되므로, 플러그인 코드에서는
 * 카탈로그를 이름으로 조회해 별칭 문자열로 찾는다.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
