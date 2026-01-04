plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "GEXF format writer for the grph ecosystem"

dependencies {
    api(project(":graph-writer-api"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}
