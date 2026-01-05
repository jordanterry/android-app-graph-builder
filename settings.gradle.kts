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
        // For Metro artifacts
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

include(":graph-model")
include(":graph-writer-api")
include(":graph-writer-gexf")
include(":graph-source-api")
include(":graph-source-metro")
include(":dagger-spi")
include(":gradle-plugin")
include(":integration-tests")
include(":samples:metro-test")
