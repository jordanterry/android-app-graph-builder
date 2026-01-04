rootProject.name = "android-app-graph-builder"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

include(":graph-model")
include(":graph-writer-api")
include(":graph-writer-gexf")
include(":dagger-spi")
include(":gradle-plugin")
include(":integration-tests")
