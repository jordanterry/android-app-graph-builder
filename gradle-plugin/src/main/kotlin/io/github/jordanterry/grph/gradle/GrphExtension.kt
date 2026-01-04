package io.github.jordanterry.grph.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Extension for configuring the Grph Dagger graph builder plugin.
 *
 * Usage in build.gradle.kts:
 * ```
 * grph {
 *     enabled.set(true)
 *     outputPath.set("build/reports/dagger-graph")
 *     outputFormat.set("gexf")
 * }
 * ```
 */
abstract class GrphExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Enable or disable the graph generation.
     * Default: true
     */
    val enabled: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)

    /**
     * Custom output path for generated graph files.
     * If not set, defaults to "grph/" in the class output directory.
     */
    val outputPath: Property<String> = objects.property(String::class.java)

    /**
     * Output format for the generated graph.
     * Currently only "gexf" is supported.
     * Default: "gexf"
     */
    val outputFormat: Property<String> = objects.property(String::class.java)
        .convention("gexf")
}
