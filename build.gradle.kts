plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

subprojects {
    group = "io.github.jordanterry.appgraph"
    version = "0.0.2"

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // Add JUnit platform launcher for Gradle 9.x compatibility
    afterEvaluate {
        if (configurations.findByName("testRuntimeOnly") != null) {
            dependencies {
                "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
            }
        }
    }

    // Configure maven-publish for publishable modules
    if (name != "integration-tests") {
        apply(plugin = "maven-publish")

        afterEvaluate {
            // Only configure if java component exists (not for gradle-plugin which uses its own publishing)
            if (components.findByName("java") != null && !pluginManager.hasPlugin("java-gradle-plugin")) {
                configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("maven") {
                            from(components["java"])

                            pom {
                                name.set(project.name)
                                description.set(project.description)
                                url.set("https://github.com/jordanterry/app-graph")

                                licenses {
                                    license {
                                        name.set("Apache License 2.0")
                                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                    }
                                }
                            }
                        }
                    }

                    repositories {
                        mavenLocal()
                    }
                }
            }
        }
    }
}
