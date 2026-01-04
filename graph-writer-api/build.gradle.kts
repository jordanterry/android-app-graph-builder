plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "Graph writer API for the grph ecosystem"

dependencies {
    api(project(":graph-model"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}
