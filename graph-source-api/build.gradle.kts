plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "Graph source API - abstraction for extracting graphs from various DI frameworks"

dependencies {
    api(project(":graph-model"))

    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}
