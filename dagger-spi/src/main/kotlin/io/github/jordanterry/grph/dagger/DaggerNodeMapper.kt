package io.github.jordanterry.grph.dagger

import dagger.spi.model.Binding
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraph.ComponentNode
import dagger.spi.model.BindingGraph.MissingBinding
import dagger.spi.model.BindingKind as DaggerBindingKind
import io.github.jordanterry.grph.model.*

/**
 * Maps Dagger SPI model objects to our abstract Graph model.
 */
class DaggerNodeMapper {

    private var nodeIdCounter = 0
    private var edgeIdCounter = 0

    // Maps Dagger keys to our node IDs for edge creation
    private val keyToNodeId = mutableMapOf<String, String>()
    private val componentToNodeId = mutableMapOf<String, String>()

    fun nextNodeId(): String = "n${nodeIdCounter++}"
    fun nextEdgeId(): String = "e${edgeIdCounter++}"

    /**
     * Get or create the node ID for a component.
     */
    fun getComponentNodeId(componentNode: ComponentNode): String {
        val key = componentNode.componentPath().toString()
        return componentToNodeId.getOrPut(key) { nextNodeId() }
    }

    /**
     * Map a Dagger ComponentNode to our ComponentNode.
     */
    fun mapComponentNode(componentNode: ComponentNode): io.github.jordanterry.grph.model.ComponentNode {
        val componentPath = componentNode.componentPath()
        val id = getComponentNodeId(componentNode)
        val pathString = componentPath.toString()

        // Extract simple name from component path string
        val simpleName = pathString
            .substringAfterLast(".")
            .replace("[", "")
            .replace("]", "")

        return ComponentNode(
            id = id,
            label = simpleName,
            qualifiedName = pathString,
            isSubcomponent = componentNode.isSubcomponent,
            scopes = componentNode.scopes().map { it.toString() },
            componentPath = pathString
        )
    }

    /**
     * Map a Dagger Binding to our BindingNode.
     */
    fun mapBindingNode(binding: Binding): BindingNode {
        val key = binding.key().toString()
        val id = nextNodeId()
        keyToNodeId[key] = id

        val contributingModule = try {
            binding.contributingModule().orElse(null)?.toString()
        } catch (e: Exception) {
            null
        }

        return BindingNode(
            id = id,
            label = simplifyKey(key),
            key = key,
            bindingKind = mapBindingKind(binding.kind()),
            scope = binding.scope().orElse(null)?.toString(),
            contributingModule = contributingModule,
            isMultibinding = isMultibinding(binding.kind())
        )
    }

    /**
     * Map a Dagger MissingBinding to our MissingBindingNode.
     */
    fun mapMissingBinding(missingBinding: BindingGraph.MissingBinding): MissingBindingNode {
        val key = missingBinding.key().toString()
        val id = nextNodeId()
        keyToNodeId[key] = id

        return MissingBindingNode(
            id = id,
            label = "[MISSING] ${simplifyKey(key)}",
            key = key
        )
    }

    /**
     * Map a Dagger DependencyEdge to our DependencyEdge.
     */
    fun mapDependencyEdge(
        edge: BindingGraph.DependencyEdge,
        bindingGraph: BindingGraph
    ): DependencyEdge? {
        val sourceKey = edge.dependencyRequest().key().toString()
        val sourceId = keyToNodeId[sourceKey]

        // Get the target node from the edge's network
        val targetNode = try {
            bindingGraph.network().incidentNodes(edge).target()
        } catch (e: Exception) {
            return null
        }

        val targetId = when (targetNode) {
            is Binding -> keyToNodeId[targetNode.key().toString()]
            is MissingBinding -> keyToNodeId[targetNode.key().toString()]
            else -> null
        }

        if (sourceId == null || targetId == null) {
            return null
        }

        return DependencyEdge(
            id = nextEdgeId(),
            source = sourceId,
            target = targetId,
            isEntryPoint = edge.isEntryPoint
        )
    }

    /**
     * Map Dagger BindingKind to our BindingKind.
     */
    private fun mapBindingKind(daggerKind: DaggerBindingKind): BindingKind {
        return when (daggerKind.name) {
            "INJECTION" -> BindingKind.INJECTION
            "PROVISION" -> BindingKind.PROVISION
            "DELEGATE" -> BindingKind.DELEGATE
            "COMPONENT" -> BindingKind.COMPONENT_PROVISION
            "COMPONENT_PROVISION" -> BindingKind.COMPONENT_PROVISION
            "COMPONENT_DEPENDENCY" -> BindingKind.COMPONENT_DEPENDENCY
            "COMPONENT_PRODUCTION" -> BindingKind.PRODUCTION
            "MULTIBOUND_SET" -> BindingKind.MULTIBOUND_SET
            "MULTIBOUND_MAP" -> BindingKind.MULTIBOUND_MAP
            "OPTIONAL" -> BindingKind.OPTIONAL
            "MEMBERS_INJECTOR" -> BindingKind.MEMBERS_INJECTOR
            "MEMBERS_INJECTION" -> BindingKind.MEMBERS_INJECTOR
            "ASSISTED_INJECTION" -> BindingKind.ASSISTED_INJECTION
            "ASSISTED_FACTORY" -> BindingKind.ASSISTED_FACTORY
            "BOUND_INSTANCE" -> BindingKind.BOUND_INSTANCE
            "SUBCOMPONENT_CREATOR" -> BindingKind.SUBCOMPONENT_CREATOR
            "PRODUCTION" -> BindingKind.PRODUCTION
            else -> BindingKind.PROVISION // Default fallback
        }
    }

    /**
     * Check if a binding kind represents a multibinding.
     */
    private fun isMultibinding(kind: DaggerBindingKind): Boolean {
        return kind.name == "MULTIBOUND_SET" || kind.name == "MULTIBOUND_MAP"
    }

    /**
     * Simplify a fully qualified key to a more readable label.
     * e.g., "com.example.foo.UserRepository" -> "UserRepository"
     */
    private fun simplifyKey(key: String): String {
        // Handle generic types like "Set<String>"
        val baseType = key.substringBefore('<')
        val simpleName = baseType.substringAfterLast('.')

        // Preserve generic parameters if present
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
