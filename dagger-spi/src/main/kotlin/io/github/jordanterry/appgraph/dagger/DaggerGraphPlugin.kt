package io.github.jordanterry.appgraph.dagger

import com.google.auto.service.AutoService
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraphPlugin
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DiagnosticReporter
import io.github.jordanterry.appgraph.model.Graph
import io.github.jordanterry.appgraph.writer.GraphWriter
import io.github.jordanterry.appgraph.writer.gexf.GexfWriter
import java.io.File
import java.io.FileOutputStream

/**
 * Dagger SPI plugin that extracts the binding graph and outputs it in GEXF format.
 *
 * This plugin is automatically discovered by Dagger's annotation processor via
 * the ServiceLoader mechanism (configured via @AutoService).
 *
 * Configuration options (via annotation processor arguments):
 * - `appgraph.output.path`: Custom output path for GEXF files
 * - `appgraph.output.format`: Output format (default: gexf)
 * - `appgraph.enabled`: Enable/disable the plugin (default: true)
 */
@AutoService(BindingGraphPlugin::class)
class DaggerGraphPlugin : BindingGraphPlugin {

    private var outputPath: String? = null
    private var outputFormat: String = "gexf"
    private var enabled: Boolean = true

    private val graphExtractor = DaggerGraphExtractor()
    private val collectedGraphs = mutableListOf<Graph>()

    override fun pluginName(): String = "AppGraphDaggerPlugin"

    override fun supportedOptions(): Set<String> = setOf(
        "appgraph.output.path",
        "appgraph.output.format",
        "appgraph.enabled"
    )

    override fun init(processingEnv: DaggerProcessingEnv, options: Map<String, String>) {
        options["appgraph.output.path"]?.let { outputPath = it }
        options["appgraph.output.format"]?.let { outputFormat = it }
        options["appgraph.enabled"]?.let { enabled = it.toBoolean() }
    }

    override fun visitGraph(bindingGraph: BindingGraph, reporter: DiagnosticReporter) {
        if (!enabled) return

        // Skip module binding graphs - we only want full component graphs
        if (bindingGraph.isModuleBindingGraph) {
            return
        }

        val graph = graphExtractor.extract(bindingGraph)

        collectedGraphs.add(graph)
        writeGraph(graph, bindingGraph)
    }

    override fun onPluginEnd() {
        // Could optionally write a merged graph here if needed
    }

    private fun writeGraph(graph: Graph, bindingGraph: BindingGraph) {
        val writer = createWriter()
        val fileName = generateFileName(bindingGraph, writer.fileExtension)

        try {
            // Write to file in build directory
            val outputDir = File(outputPath ?: "build/generated/appgraph")
            outputDir.mkdirs()
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { outputStream ->
                writer.write(graph, outputStream)
            }

            System.out.println("[AppGraphDaggerPlugin] Generated graph: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            // Log but don't fail compilation
            System.err.println("[AppGraphDaggerPlugin] Failed to write graph: ${e.message}")
        }
    }

    private fun createWriter(): GraphWriter {
        return when (outputFormat) {
            "gexf" -> GexfWriter(prettyPrint = true)
            else -> GexfWriter(prettyPrint = true) // Default to GEXF
        }
    }

    private fun generateFileName(bindingGraph: BindingGraph, extension: String): String {
        val componentPath = bindingGraph.rootComponentNode().componentPath()
        val componentName = componentPath.toString()
            .substringAfterLast(".")
            .replace("[", "")
            .replace("]", "")

        return "${componentName}.$extension"
    }
}
