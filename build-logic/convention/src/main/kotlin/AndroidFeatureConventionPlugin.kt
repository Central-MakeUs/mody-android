import com.makeus.mody.convention.libs
import com.makeus.mody.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * :feature:* 의 공통 골격.
 *
 * 의존성 방향(feature → core 단방향)을 여기서 한 번에 고정한다. feature 가
 * :core:data / :core:network 를 보지 못하는 것도 이 목록에 없기 때문이다.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mody.android.library.compose")
        pluginManager.apply("mody.android.hilt")

        dependencies {
            add("implementation", project(":core:common-ui"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:navigation"))
            add("implementation", project(":core:domain"))

            add("implementation", libs.library("androidx-navigation-compose"))
            add("implementation", libs.library("hilt-navigation-compose"))
            add("implementation", libs.library("kotlinx-coroutines-android"))
        }
    }
}
