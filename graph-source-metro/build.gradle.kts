plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

description = "Metro graph source - reads Metro DI JSON metadata and converts to Graph model"

dependencies {
    api(project(":graph-source-api"))
    api(project(":graph-model"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.truth)
}
