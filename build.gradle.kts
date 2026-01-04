plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

subprojects {
    group = "io.github.jordanterry.appgraph"
    version = "0.0.1"

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
