pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HabitFlow"
include(":app")
// Thêm
include(":feature:goals")
include(":core:model")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:database")

