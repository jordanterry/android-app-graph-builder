plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":dagger-spi"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("appGraph") {
            id = "io.github.jordanterry.appgraph"
            implementationClass = "io.github.jordanterry.appgraph.gradle.AppGraphPlugin"
            displayName = "AppGraph Dagger Graph Builder"
            description = "Generates GEXF graph visualizations of Dagger dependency graphs"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

// Functional tests source set
val functionalTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val functionalTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val functionalTestTask = tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath + files(tasks.named("pluginUnderTestMetadata"))
    useJUnitPlatform()
}

tasks.check {
    dependsOn(functionalTestTask)
}

dependencies {
    functionalTestImplementation(libs.junit.jupiter)
    functionalTestImplementation(libs.truth)
    functionalTestImplementation(gradleTestKit())
}
