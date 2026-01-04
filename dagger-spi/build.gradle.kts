plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.dagger.spi)
    implementation(libs.auto.service.annotations)
    kapt(libs.auto.service)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.dagger)
    testImplementation(libs.dagger.compiler)
}
