package com.makeus.mody.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * CLAUDE.md 의 Clean Architecture + MVI 규칙을 코드로 검사한다.
 *
 * 왜 필요한가 — 규칙이 글로만 있으면 사람이 읽고 지켜야 하고, AI 가 생성한 코드는
 * 특히 잘 벗어난다. 벗어난 게 컴파일은 되기 때문에 리뷰에서만 걸리고, 리뷰가 놓치면
 * 그대로 남는다.
 *
 * 모듈 의존성 방향(`:feature:*` 가 `:core:data` 를 못 봄)은 Gradle 이 컴파일 타임에
 * 이미 막는다. 여기서 보는 건 **같은 모듈 안에서 지켜야 하는 규칙** — 컴파일러가
 * 알 수 없는 것들이다.
 */
class ArchitectureRuleTest {

    // --- MVI 계약 ---

    @Test
    fun `State 는 data class 이고 UiState 를 구현한다`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("State")
            .filter { it.resideInPackage("..contract..") }
            .assertTrue {
                it.hasDataModifier && it.parents().any { parent -> parent.name == "UiState" }
            }
    }

    @Test
    fun `Intent 는 sealed 이고 UiIntent 를 구현한다`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("Intent")
            .filter { it.resideInPackage("..contract..") }
            .assertTrue {
                it.hasSealedModifier && it.parents().any { parent -> parent.name == "UiIntent" }
            }
    }

    /**
     * State 는 불변이어야 한다. `var` 프로퍼티가 있으면 setState 를 거치지 않고
     * 바꿀 수 있어 단방향 흐름이 깨진다 — 그렇게 바꾼 값은 recompose 도 안 된다.
     */
    @Test
    fun `State 에 var 프로퍼티가 없다`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("State")
            .filter { it.resideInPackage("..contract..") }
            .assertFalse { klass -> klass.properties().any { it.isVar } }
    }

    // --- ViewModel ---

    /**
     * feature 의 ViewModel 은 BaseViewModel 을 상속한다 — setState/onIntent 가 거기 있다.
     *
     * `:presentation` 의 앱 셸 ViewModel(MainViewModel 등)은 화면 상태가 없어 제외한다.
     */
    @Test
    fun `feature 의 ViewModel 은 BaseViewModel 을 상속한다`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .filter { it.resideInPackage("com.makeus.mody.feature..") }
            // BaseViewModel 은 제네릭이라 parent.name 이 "BaseViewModel<LoginState, LoginIntent>" 다.
            .assertTrue {
                it.parents().any { parent -> parent.name.substringBefore('<') == "BaseViewModel" }
            }
    }

    @Test
    fun `ViewModel 은 HiltViewModel 로 주입받는다`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .filter { it.resideInPackage("com.makeus.mody.feature..") }
            .assertTrue { it.hasAnnotation { annotation -> annotation.name == "HiltViewModel" } }
    }

    // --- Screen (UI 레이어) ---

    /**
     * 화면 이동은 ViewModel 의 navigationHelper 로만 한다.
     *
     * Screen 이 NavController 를 직접 만지면 이동 조건이 UI 에 흩어져, 같은 화면으로
     * 가는 경로마다 백스택 처리가 달라진다.
     */
    @Test
    fun `Screen 은 NavController 를 직접 다루지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.name.endsWith("Screen") }
            .assertFalse { file ->
                file.imports.any { it.name.contains("NavController") }
            }
    }

    /**
     * Screen 은 렌더링만 한다. Repository 를 직접 부르면 그 호출은 상태 흐름 밖에서
     * 일어나 로딩·실패 처리가 없고, 테스트도 불가능해진다.
     */
    @Test
    fun `Screen 은 Repository 를 직접 참조하지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.name.endsWith("Screen") }
            .assertFalse { file ->
                file.imports.any { it.name.contains(".repository.") }
            }
    }

    // --- 레이어 경계 ---

    /**
     * UI 레이어는 구현체를 모른다.
     *
     * Gradle 의존성이 이미 막고 있지만, 모듈 구조는 나중에 누가 `implementation(project(...))`
     * 한 줄로 열 수 있다. 그때 이 테스트가 먼저 깨진다.
     */
    @Test
    fun `feature 와 presentation 은 data network 구현체를 참조하지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter {
                it.packagee?.name?.startsWith("com.makeus.mody.feature") == true ||
                    it.packagee?.name?.startsWith("com.makeus.mody.presentation") == true
            }
            .assertFalse { file ->
                file.imports.any {
                    it.name.startsWith("com.makeus.mody.core.data") ||
                        it.name.startsWith("com.makeus.mody.core.network")
                }
            }
    }

    /**
     * `:core:domain` 은 아무 데도 의존하지 않는다 — 비즈니스 규칙이 프레임워크에 묶이면
     * 테스트할 때 기기가 필요해진다.
     *
     * `javax.inject` 는 DI 표준 애노테이션이라 예외로 둔다(런타임 의존이 아니다).
     */
    @Test
    fun `domain 은 안드로이드 프레임워크에 의존하지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.packagee?.name?.startsWith("com.makeus.mody.core.domain") == true }
            .assertFalse { file ->
                file.imports.any {
                    it.name.startsWith("android.") || it.name.startsWith("androidx.")
                }
            }
    }

    // --- 네이밍 ---

    @Test
    fun `Repository 구현체는 Impl 로 끝나고 Singleton 이다`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { klass ->
                klass.parents().any { it.name.endsWith("Repository") }
            }
            .assertTrue {
                it.name.endsWith("RepositoryImpl") &&
                    it.hasAnnotation { annotation -> annotation.name == "Singleton" }
            }
    }
}
