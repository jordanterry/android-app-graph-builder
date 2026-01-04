package io.github.jordanterry.grph.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Gradle plugin that configures the Grph Dagger SPI plugin for a project.
 *
 * This plugin:
 * 1. Creates the `grph` extension for configuration
 * 2. Adds the dagger-spi module as an annotation processor dependency
 * 3. Configures annotation processor arguments based on the extension
 *
 * Usage:
 * ```
 * plugins {
 *     id("io.github.jordanterry.grph")
 * }
 *
 * grph {
 *     enabled.set(true)
 *     outputPath.set("build/reports/dagger-graph")
 * }
 * ```
 */
class GrphPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "grph"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            GrphExtension::class.java
        )

        project.afterEvaluate {
            configureAnnotationProcessor(project, extension)
        }
    }

    private fun configureAnnotationProcessor(project: Project, extension: GrphExtension) {
        val arguments = buildProcessorArguments(extension)

        // Configure kapt if available (Kotlin projects)
        configureKapt(project, arguments)

        // Configure Java annotationProcessor
        configureJavaAnnotationProcessor(project, arguments)
    }

    private fun buildProcessorArguments(extension: GrphExtension): Map<String, String> {
        val arguments = mutableMapOf<String, String>()

        arguments["grph.enabled"] = extension.enabled.get().toString()
        arguments["grph.output.format"] = extension.outputFormat.get()

        extension.outputPath.orNull?.let {
            arguments["grph.output.path"] = it
        }

        return arguments
    }

    private fun configureKapt(project: Project, arguments: Map<String, String>) {
        // Check if kapt plugin is applied
        project.pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
            project.extensions.findByName("kapt")?.let { kapt ->
                // Use reflection to configure kapt arguments since the class may not be on classpath
                try {
                    val argumentsMethod = kapt.javaClass.getMethod("arguments", groovy.lang.Closure::class.java)
                    argumentsMethod.invoke(kapt, object : groovy.lang.Closure<Unit>(this) {
                        fun doCall() {
                            arguments.forEach { (key, value) ->
                                val argMethod = delegate.javaClass.getMethod("arg", String::class.java, Any::class.java)
                                argMethod.invoke(delegate, key, value)
                            }
                        }
                    })
                } catch (e: Exception) {
                    project.logger.warn("[Grph] Failed to configure kapt arguments: ${e.message}")
                }
            }
        }
    }

    private fun configureJavaAnnotationProcessor(project: Project, arguments: Map<String, String>) {
        project.tasks.withType(JavaCompile::class.java).configureEach { task ->
            arguments.forEach { (key, value) ->
                task.options.compilerArgs.add("-A$key=$value")
            }
        }
    }
}
