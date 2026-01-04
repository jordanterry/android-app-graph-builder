package io.github.jordanterry.appgraph.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Gradle plugin that configures the AppGraph Dagger SPI plugin for a project.
 *
 * This plugin:
 * 1. Creates the `appGraph` extension for configuration
 * 2. Adds the dagger-spi module as an annotation processor dependency
 * 3. Configures annotation processor arguments based on the extension
 *
 * Usage:
 * ```
 * plugins {
 *     id("io.github.jordanterry.appgraph")
 * }
 *
 * appGraph {
 *     enabled.set(true)
 *     outputPath.set("build/reports/dagger-graph")
 * }
 * ```
 */
class AppGraphPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "appGraph"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            AppGraphExtension::class.java
        )

        project.afterEvaluate {
            configureAnnotationProcessor(project, extension)
        }
    }

    private fun configureAnnotationProcessor(project: Project, extension: AppGraphExtension) {
        val arguments = buildProcessorArguments(extension)

        // Configure kapt if available (Kotlin projects)
        configureKapt(project, arguments)

        // Configure Java annotationProcessor
        configureJavaAnnotationProcessor(project, arguments)
    }

    private fun buildProcessorArguments(extension: AppGraphExtension): Map<String, String> {
        val arguments = mutableMapOf<String, String>()

        arguments["appgraph.enabled"] = extension.enabled.get().toString()
        arguments["appgraph.output.format"] = extension.outputFormat.get()

        extension.outputPath.orNull?.let {
            arguments["appgraph.output.path"] = it
        }

        return arguments
    }

    private fun configureKapt(project: Project, arguments: Map<String, String>) {
        // Check if kapt plugin is applied
        project.pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
            // Configure kapt arguments via the standard kaptOptions approach
            project.tasks.matching { it.name.startsWith("kapt") && it.name.endsWith("Kotlin") }.configureEach { task ->
                arguments.forEach { (key, value) ->
                    // Add arguments via task inputs
                    task.inputs.property(key, value)
                }
            }

            // Also add arguments to kaptGenerateStubs tasks
            project.tasks.matching { it.name.contains("kaptGenerateStubs") }.configureEach { task ->
                arguments.forEach { (key, value) ->
                    task.inputs.property(key, value)
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
