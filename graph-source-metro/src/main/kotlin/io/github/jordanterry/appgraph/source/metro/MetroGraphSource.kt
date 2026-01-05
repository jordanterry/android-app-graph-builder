package io.github.jordanterry.appgraph.source.metro

import io.github.jordanterry.appgraph.model.Graph
import io.github.jordanterry.appgraph.source.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

/**
 * GraphSource implementation that reads Metro's JSON metadata files.
 *
 * Metro outputs graph metadata to:
 * {reportsDestination}/{sourceSet}/graph-metadata/graph-{GraphName}.json
 */
class MetroGraphSource : GraphSource {

    override val sourceType: String = "metro"
    override val displayName: String = "Metro DI"

    private val extractor = MetroGraphExtractor()

    override fun extract(input: GraphSourceInput): GraphSourceResult {
        val metroInput = input as? MetroGraphSourceInput
            ?: return GraphSourceResult.Error("Invalid input type for Metro source")

        val jsonFiles = findMetroJsonFiles(metroInput.paths)

        if (jsonFiles.isEmpty()) {
            return GraphSourceResult.Error(
                "No Metro graph metadata files found in: ${metroInput.paths}"
            )
        }

        val graphs = mutableListOf<Graph>()
        val errors = mutableListOf<String>()

        for (jsonFile in jsonFiles) {
            try {
                val graph = extractor.extractFromFile(jsonFile)
                graphs.add(graph)
            } catch (e: Exception) {
                errors.add("Failed to parse $jsonFile: ${e.message}")
            }
        }

        return when {
            graphs.isEmpty() -> GraphSourceResult.Error(
                "Failed to extract any graphs. Errors: ${errors.joinToString("; ")}"
            )
            errors.isNotEmpty() -> GraphSourceResult.Partial(graphs, errors)
            else -> GraphSourceResult.Success(graphs)
        }
    }

    /**
     * Find Metro JSON metadata files in the given paths.
     * Looks for pattern: graph-metadata/graph-*.json or any .json file
     */
    private fun findMetroJsonFiles(paths: List<Path>): List<Path> {
        return paths.flatMap { path ->
            when {
                !path.exists() -> emptyList()
                path.isDirectory() -> {
                    Files.walk(path)
                        .filter { it.extension == "json" }
                        .filter {
                            it.parent?.fileName?.toString() == "graph-metadata" ||
                                it.fileName.toString().startsWith("graph-")
                        }
                        .toList()
                }
                path.extension == "json" -> listOf(path)
                else -> emptyList()
            }
        }
    }
}
