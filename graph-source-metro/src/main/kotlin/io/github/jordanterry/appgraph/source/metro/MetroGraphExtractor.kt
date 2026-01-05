package io.github.jordanterry.appgraph.source.metro

import io.github.jordanterry.appgraph.model.*
import io.github.jordanterry.appgraph.source.metro.model.MetroGraphMetadata
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.readText

/**
 * Extracts a Graph from Metro's JSON metadata files.
 */
class MetroGraphExtractor {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Extract a Graph from a Metro JSON metadata file.
     */
    fun extractFromFile(path: Path): Graph {
        val content = path.readText()
        return extractFromJson(content)
    }

    /**
     * Extract a Graph from a JSON string.
     */
    fun extractFromJson(jsonContent: String): Graph {
        val metadata = json.decodeFromString<MetroGraphMetadata>(jsonContent)
        return extractFromMetadata(metadata)
    }

    /**
     * Extract a Graph from parsed Metro metadata.
     */
    fun extractFromMetadata(metadata: MetroGraphMetadata): Graph {
        val mapper = MetroNodeMapper()

        val nodes = mutableListOf<Node>()
        val edges = mutableListOf<Edge>()

        // Create a ComponentNode for the graph itself
        val graphNode = mapper.mapGraphToComponent(metadata)
        nodes.add(graphNode)

        // Extract binding nodes
        for (binding in metadata.bindings) {
            val bindingNode = mapper.mapBinding(binding)
            nodes.add(bindingNode)

            // Create dependency edges
            for (dependency in binding.dependencies) {
                val edge = mapper.mapDependencyEdge(binding, dependency)
                edges.add(edge)
            }
        }

        // Create entry point edges from graph accessors
        for (accessor in metadata.roots.accessors) {
            val edge = mapper.mapAccessorEdge(metadata.graph, accessor)
            edges.add(edge)
        }

        // Create entry point edges from injectors
        for (injector in metadata.roots.injectors) {
            edges.add(
                DependencyEdge(
                    id = mapper.nextEdgeId(),
                    source = mapper.getNodeIdForKey(metadata.graph),
                    target = mapper.getNodeIdForKey(injector.key),
                    isEntryPoint = true,
                    attributes = mapOf("type" to "injector")
                )
            )
        }

        return Graph(
            id = metadata.graph.replace(".", "_"),
            name = metadata.graph,
            nodes = nodes,
            edges = edges,
            metadata = GraphMetadata(
                creator = "appgraph-metro-source",
                description = "Metro dependency graph for ${metadata.graph}",
                createdAt = Instant.now().toString()
            )
        )
    }
}
