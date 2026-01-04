plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
}

description = "Integration tests for the Dagger SPI plugin using compile-testing"

dependencies {
    implementation(project(":graph-model"))
    implementation(project(":graph-writer-api"))
    implementation(project(":graph-writer-gexf"))
    implementation(project(":dagger-spi"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.dagger)
    testImplementation(libs.dagger.compiler)
}
