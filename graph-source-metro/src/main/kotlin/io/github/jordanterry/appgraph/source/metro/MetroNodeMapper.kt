package io.github.jordanterry.appgraph.source.metro

import io.github.jordanterry.appgraph.model.*
import io.github.jordanterry.appgraph.source.metro.model.*

/**
 * Maps Metro metadata concepts to our Graph model nodes and edges.
 */
class MetroNodeMapper {

    private var nodeIdCounter = 0
    private var edgeIdCounter = 0
    private val keyToNodeId = mutableMapOf<String, String>()

    fun nextNodeId(): String = "n${nodeIdCounter++}"
    fun nextEdgeId(): String = "e${edgeIdCounter++}"

    fun getNodeIdForKey(key: String): String =
        keyToNodeId.getOrPut(key) { nextNodeId() }

    /**
     * Map the Metro graph itself to a ComponentNode.
     */
    fun mapGraphToComponent(metadata: MetroGraphMetadata): ComponentNode {
        val id = getNodeIdForKey(metadata.graph)
        val simpleName = metadata.graph.substringAfterLast(".")

        return ComponentNode(
            id = id,
            label = simpleName,
            qualifiedName = metadata.graph,
            isSubcomponent = metadata.extensions.accessors.isNotEmpty(),
            scopes = metadata.scopes,
            componentPath = metadata.graph
        )
    }

    /**
     * Map a Metro binding to a BindingNode.
     */
    fun mapBinding(binding: MetroBinding): BindingNode {
        val id = getNodeIdForKey(binding.key)

        return BindingNode(
            id = id,
            label = simplifyKey(binding.key),
            key = binding.key,
            bindingKind = mapBindingKind(binding.bindingKind),
            scope = if (binding.isScoped) "Scoped" else null,
            contributingModule = extractModule(binding.origin),
            isMultibinding = binding.multibinding != null,
            attributes = buildMap {
                if (binding.isSynthetic) put("synthetic", "true")
                binding.nameHint?.let { put("nameHint", it) }
                binding.declaration?.let { put("declaration", it) }
                binding.aliasTarget?.let { put("aliasTarget", it) }
            }
        )
    }

    /**
     * Map a dependency relationship to a DependencyEdge.
     */
    fun mapDependencyEdge(
        binding: MetroBinding,
        dependency: MetroDependency
    ): DependencyEdge {
        val sourceId = getNodeIdForKey(binding.key)
        val targetId = getNodeIdForKey(dependency.key)

        return DependencyEdge(
            id = nextEdgeId(),
            source = sourceId,
            target = targetId,
            isEntryPoint = false,
            attributes = buildMap {
                if (dependency.hasDefault) put("hasDefault", "true")
                if (dependency.isAssisted) put("isAssisted", "true")
            }
        )
    }

    /**
     * Create an entry point edge from graph to accessor.
     */
    fun mapAccessorEdge(graphKey: String, accessor: MetroAccessor): DependencyEdge {
        val sourceId = getNodeIdForKey(graphKey)
        val targetId = getNodeIdForKey(accessor.key)

        return DependencyEdge(
            id = nextEdgeId(),
            source = sourceId,
            target = targetId,
            isEntryPoint = true,
            attributes = buildMap {
                if (accessor.isDeferrable) put("deferrable", "true")
            }
        )
    }

    /**
     * Map Metro binding kind string to our BindingKind enum.
     */
    private fun mapBindingKind(metroKind: String): BindingKind {
        return when (metroKind) {
            "ConstructorInjected" -> BindingKind.INJECTION
            "Provided" -> BindingKind.PROVISION
            "Bound", "Alias" -> BindingKind.DELEGATE
            "BoundInstance" -> BindingKind.BOUND_INSTANCE
            "IntoSet" -> BindingKind.MULTIBOUND_SET
            "IntoMap" -> BindingKind.MULTIBOUND_MAP
            "Assisted" -> BindingKind.ASSISTED_INJECTION
            "AssistedFactory" -> BindingKind.ASSISTED_FACTORY
            "GraphAccessor" -> BindingKind.COMPONENT_PROVISION
            "Optional" -> BindingKind.OPTIONAL
            else -> BindingKind.PROVISION
        }
    }

    /**
     * Extract module name from origin string (e.g., "AppGraph.kt:36:1" -> "AppGraph")
     */
    private fun extractModule(origin: String?): String? {
        if (origin == null) return null
        val fileName = origin.substringBefore(":")
        return fileName.substringBefore(".")
    }

    /**
     * Simplify a fully qualified key to a more readable label.
     */
    private fun simplifyKey(key: String): String {
        val baseType = key.substringBefore('<')
        val simpleName = baseType.substringAfterLast('.')

        val genericPart = if (key.contains('<')) {
            val genericContent = key.substringAfter('<').substringBeforeLast('>')
            val simplifiedGeneric = genericContent.split(',').joinToString(", ") {
                it.trim().substringAfterLast('.')
            }
            "<$simplifiedGeneric>"
        } else {
            ""
        }

        return "$simpleName$genericPart"
    }
}
