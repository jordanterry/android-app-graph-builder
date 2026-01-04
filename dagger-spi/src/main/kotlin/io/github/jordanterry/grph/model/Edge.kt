package io.github.jordanterry.grph.model

/**
 * Base interface for all graph edges.
 * Each edge connects a source node to a target node.
 */
sealed interface Edge {
    val id: String
    val source: String
    val target: String
    val type: EdgeType
    val attributes: Map<String, String>
}

/**
 * Types of edges that can appear in the graph.
 */
enum class EdgeType {
    /** Dependency from one binding to another */
    DEPENDENCY,
    /** Parent component to child subcomponent relationship */
    COMPONENT_HIERARCHY,
    /** Component/module to included module relationship */
    MODULE_INCLUSION
}

/**
 * Represents a dependency edge between bindings.
 * The source binding depends on the target binding.
 */
data class DependencyEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap(),
    val isEntryPoint: Boolean = false
) : Edge {
    override val type: EdgeType = EdgeType.DEPENDENCY
}

/**
 * Represents a component hierarchy edge.
 * The source is the parent component, target is the child subcomponent.
 */
data class ComponentHierarchyEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap()
) : Edge {
    override val type: EdgeType = EdgeType.COMPONENT_HIERARCHY
}

/**
 * Represents a module inclusion edge.
 * The source component/module includes the target module.
 */
data class ModuleInclusionEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap()
) : Edge {
    override val type: EdgeType = EdgeType.MODULE_INCLUSION
}
