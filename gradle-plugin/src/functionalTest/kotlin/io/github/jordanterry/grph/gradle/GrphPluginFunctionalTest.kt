package io.github.jordanterry.grph.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GrphPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private val buildFile: File
        get() = File(projectDir, "build.gradle.kts")

    private val settingsFile: File
        get() = File(projectDir, "settings.gradle.kts")

    @BeforeEach
    fun setup() {
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())
    }

    @Test
    fun `plugin applies without error`() {
        buildFile.writeText("""
            plugins {
                kotlin("jvm") version "1.9.24"
                kotlin("kapt") version "1.9.24"
                id("io.github.jordanterry.grph")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("com.google.dagger:dagger:2.51.1")
                kapt("com.google.dagger:dagger-compiler:2.51.1")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `extension configures output path`() {
        buildFile.writeText("""
            plugins {
                kotlin("jvm") version "1.9.24"
                kotlin("kapt") version "1.9.24"
                id("io.github.jordanterry.grph")
            }

            repositories {
                mavenCentral()
            }

            grph {
                outputPath.set("custom/output")
            }

            dependencies {
                implementation("com.google.dagger:dagger:2.51.1")
                kapt("com.google.dagger:dagger-compiler:2.51.1")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .build()

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `extension enabled flag can be set`() {
        buildFile.writeText("""
            plugins {
                kotlin("jvm") version "1.9.24"
                kotlin("kapt") version "1.9.24"
                id("io.github.jordanterry.grph")
            }

            repositories {
                mavenCentral()
            }

            grph {
                enabled.set(false)
            }

            dependencies {
                implementation("com.google.dagger:dagger:2.51.1")
                kapt("com.google.dagger:dagger-compiler:2.51.1")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .build()

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `compiles project with Dagger component successfully`() {
        buildFile.writeText("""
            plugins {
                kotlin("jvm") version "1.9.24"
                kotlin("kapt") version "1.9.24"
                id("io.github.jordanterry.grph")
            }

            repositories {
                mavenCentral()
            }

            grph {
                outputPath.set("build/reports/grph")
            }

            dependencies {
                implementation("com.google.dagger:dagger:2.51.1")
                kapt("com.google.dagger:dagger-compiler:2.51.1")
            }
        """.trimIndent())

        // Create a simple Dagger component
        val srcDir = File(projectDir, "src/main/kotlin/com/test")
        srcDir.mkdirs()
        File(srcDir, "TestComponent.kt").writeText("""
            package com.test

            import dagger.Component
            import javax.inject.Inject

            class UserRepository @Inject constructor()

            @Component
            interface TestComponent {
                fun userRepository(): UserRepository
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("build", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Verify compilation succeeds with the plugin applied
        assertThat(result.task(":build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":kaptKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
