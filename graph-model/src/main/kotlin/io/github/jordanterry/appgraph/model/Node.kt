package io.github.jordanterry.appgraph.model

/**
 * Base interface for all graph nodes.
 * Each node has a unique ID, display label, type, and optional attributes.
 */
sealed interface Node {
    val id: String
    val label: String
    val type: NodeType
    val attributes: Map<String, String>
}

/**
 * Types of nodes that can appear in the graph.
 */
enum class NodeType {
    COMPONENT,
    BINDING,
    MODULE,
    MISSING_BINDING
}

/**
 * Represents a Dagger component or subcomponent.
 */
data class ComponentNode(
    override val id: String,
    override val label: String,
    override val attributes: Map<String, String> = emptyMap(),
    val qualifiedName: String,
    val isSubcomponent: Boolean,
    val scopes: List<String>,
    val componentPath: String
) : Node {
    override val type: NodeType = NodeType.COMPONENT
}

/**
 * Represents a binding in the Dagger graph.
 * This could be from @Inject constructors, @Provides methods, @Binds methods, etc.
 */
data class BindingNode(
    override val id: String,
    override val label: String,
    override val attributes: Map<String, String> = emptyMap(),
    val key: String,
    val bindingKind: BindingKind,
    val scope: String?,
    val contributingModule: String?,
    val isMultibinding: Boolean
) : Node {
    override val type: NodeType = NodeType.BINDING
}

/**
 * Represents a Dagger module.
 */
data class ModuleNode(
    override val id: String,
    override val label: String,
    override val attributes: Map<String, String> = emptyMap(),
    val qualifiedName: String,
    val isAbstract: Boolean,
    val includes: List<String>
) : Node {
    override val type: NodeType = NodeType.MODULE
}

/**
 * Represents a missing binding (unresolved dependency).
 */
data class MissingBindingNode(
    override val id: String,
    override val label: String,
    override val attributes: Map<String, String> = emptyMap(),
    val key: String
) : Node {
    override val type: NodeType = NodeType.MISSING_BINDING
}

/**
 * Types of bindings in Dagger.
 */
enum class BindingKind {
    /** Binding from an @Inject-annotated constructor */
    INJECTION,
    /** Binding from an @Provides method */
    PROVISION,
    /** Binding from a @Binds method */
    DELEGATE,
    /** Component provision method */
    COMPONENT_PROVISION,
    /** Component dependency */
    COMPONENT_DEPENDENCY,
    /** Set multibinding */
    MULTIBOUND_SET,
    /** Map multibinding */
    MULTIBOUND_MAP,
    /** Optional binding */
    OPTIONAL,
    /** MembersInjector binding */
    MEMBERS_INJECTOR,
    /** Assisted injection binding */
    ASSISTED_INJECTION,
    /** Assisted factory binding */
    ASSISTED_FACTORY,
    /** Bound instance */
    BOUND_INSTANCE,
    /** Subcomponent creator binding */
    SUBCOMPONENT_CREATOR,
    /** Producer binding (for Dagger Producers) */
    PRODUCTION
}
