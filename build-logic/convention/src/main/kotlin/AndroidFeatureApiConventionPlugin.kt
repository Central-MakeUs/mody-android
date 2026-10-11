import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * :feature:*:api 의 공통 골격.
 *
 * 다른 feature 가 이 feature 로 이동할 때 필요한 것(Route, Route 인자 타입)만 담는다.
 * 화면·ViewModel 은 :impl 에 있고, 다른 feature 는 :impl 을 볼 수 없다 — 그래서
 * feature 끼리는 이 모듈로만 엮인다.
 *
 * Route 가 :core:navigation 의 Route 를 구현하므로 그 모듈은 api.
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mody.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("api", project(":core:navigation"))
        }
    }
}
