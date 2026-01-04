package io.github.jordanterry.grph.model

import java.time.Instant

/**
 * Represents a complete graph of nodes and edges.
 * This is the primary data structure output by graph extractors.
 */
data class Graph(
    val id: String,
    val name: String,
    val nodes: List<Node>,
    val edges: List<Edge>,
    val metadata: GraphMetadata = GraphMetadata()
) {
    val directed: Boolean = true
}

/**
 * Metadata associated with a graph.
 */
data class GraphMetadata(
    val creator: String = "grph",
    val description: String = "",
    val createdAt: String = Instant.now().toString()
)

/**
 * Attribute definitions for GEXF output.
 * Defines the schema for node/edge attributes.
 */
data class AttributeDefinition(
    val id: String,
    val title: String,
    val type: AttributeType,
    val defaultValue: String? = null
)

/**
 * Supported attribute types in GEXF format.
 */
enum class AttributeType {
    STRING,
    INTEGER,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    DATE
}
