package io.github.jordanterry.appgraph.model

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
    MODULE_INCLUSION,
    /** Binding to its providing module */
    BINDING_TO_MODULE,
    /** Component to binding relationship (for entry points) */
    COMPONENT_TO_BINDING,
    /** Component owns/provides this binding (installed in this component) */
    BINDING_OWNERSHIP
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

/**
 * Represents an edge from a binding to its providing module.
 * The source is the binding, target is the module.
 */
data class BindingToModuleEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap()
) : Edge {
    override val type: EdgeType = EdgeType.BINDING_TO_MODULE
}

/**
 * Represents an edge from a component to a binding it exposes.
 * The source is the component, target is the binding (entry point).
 */
data class ComponentToBindingEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap()
) : Edge {
    override val type: EdgeType = EdgeType.COMPONENT_TO_BINDING
}

/**
 * Represents ownership of a binding by a component.
 * The source is the component that owns/installs the binding, target is the binding.
 * This applies to all bindings (@Inject, @Provides, @Binds) showing which component
 * they are installed in based on their scope or module installation.
 */
data class BindingOwnershipEdge(
    override val id: String,
    override val source: String,
    override val target: String,
    override val attributes: Map<String, String> = emptyMap()
) : Edge {
    override val type: EdgeType = EdgeType.BINDING_OWNERSHIP
}
