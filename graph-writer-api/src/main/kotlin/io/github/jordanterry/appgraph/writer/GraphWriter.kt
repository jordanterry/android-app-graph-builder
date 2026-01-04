package io.github.jordanterry.appgraph.writer

import io.github.jordanterry.appgraph.model.Graph
import java.io.OutputStream
import java.nio.file.Path

/**
 * Interface for writing graphs to various output formats.
 * Implementations can output to GEXF, JSON, DOT, etc.
 */
interface GraphWriter {
    /** The name of the format (e.g., "GEXF", "JSON") */
    val formatName: String

    /** The file extension for this format (e.g., "gexf", "json") */
    val fileExtension: String

    /**
     * Writes the graph to an output stream.
     *
     * @param graph The graph to write
     * @param output The output stream to write to
     */
    fun write(graph: Graph, output: OutputStream)

    /**
     * Writes the graph to a file path.
     *
     * @param graph The graph to write
     * @param outputPath The path to write to
     */
    fun write(graph: Graph, outputPath: Path) {
        outputPath.toFile().outputStream().use { write(graph, it) }
    }
}
