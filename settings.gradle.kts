pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }
}

rootProject.name = "mody"
include(":app")
include(":presentation")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:common-ui")
include(":core:designsystem")
include(":core:navigation")
include(":feature:auth")
include(":feature:onboarding")
include(":feature:group")
