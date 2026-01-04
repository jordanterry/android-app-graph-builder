plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "Core graph model abstractions for the grph ecosystem"

dependencies {
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}
