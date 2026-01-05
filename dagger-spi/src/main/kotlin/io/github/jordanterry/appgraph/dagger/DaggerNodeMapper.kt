package io.github.jordanterry.appgraph.dagger

import dagger.spi.model.Binding
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraph.ComponentNode
import dagger.spi.model.BindingGraph.MissingBinding
import dagger.spi.model.BindingKind as DaggerBindingKind
import io.github.jordanterry.appgraph.model.*

/**
 * Maps Dagger SPI model objects to our abstract Graph model.
 */
class DaggerNodeMapper {

    private var nodeIdCounter = 0
    private var edgeIdCounter = 0

    // Maps Dagger keys to our node IDs for edge creation
    private val keyToNodeId = mutableMapOf<String, String>()
    private val componentToNodeId = mutableMapOf<String, String>()
    private val moduleToNodeId = mutableMapOf<String, String>()

    // Track module information for creating ModuleNodes
    private val moduleBindingCounts = mutableMapOf<String, Int>()
    private val moduleToComponents = mutableMapOf<String, MutableSet<String>>()

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
    fun mapComponentNode(componentNode: ComponentNode): io.github.jordanterry.appgraph.model.ComponentNode {
        val componentPath = componentNode.componentPath()
        val id = getComponentNodeId(componentNode)
        val pathString = componentPath.toString()
            .replace("[", "")
            .replace("]", "")

        return ComponentNode(
            id = id,
            label = pathString,
            qualifiedName = pathString,
            isSubcomponent = componentNode.isSubcomponent,
            scopes = componentNode.scopes().map { it.toString() },
            componentPath = pathString
        )
    }

    /**
     * Map a Dagger Binding to our BindingNode.
     */
    fun mapBindingNode(binding: Binding, componentPath: String? = null): BindingNode {
        val key = binding.key().toString()
        val id = nextNodeId()
        keyToNodeId[key] = id

        val contributingModule = try {
            binding.contributingModule().orElse(null)?.toString()
        } catch (e: Exception) {
            null
        }

        // Track module usage for later ModuleNode creation
        if (contributingModule != null) {
            moduleBindingCounts[contributingModule] = (moduleBindingCounts[contributingModule] ?: 0) + 1
            if (componentPath != null) {
                moduleToComponents.getOrPut(contributingModule) { mutableSetOf() }.add(componentPath)
            }
        }

        return BindingNode(
            id = id,
            label = key,
            key = key,
            bindingKind = mapBindingKind(binding.kind()),
            scope = binding.scope().orElse(null)?.toString(),
            contributingModule = contributingModule,
            isMultibinding = isMultibinding(binding.kind()),
            componentPath = componentPath
        )
    }

    /**
     * Get or create the node ID for a module.
     */
    fun getModuleNodeId(moduleName: String): String {
        return moduleToNodeId.getOrPut(moduleName) { nextNodeId() }
    }

    /**
     * Get the binding node ID for a key.
     */
    fun getBindingNodeId(key: String): String? = keyToNodeId[key]

    /**
     * Create ModuleNodes from all tracked modules.
     */
    fun createModuleNodes(): List<ModuleNode> {
        return moduleBindingCounts.map { (moduleName, bindingCount) ->
            val id = getModuleNodeId(moduleName)
            ModuleNode(
                id = id,
                label = moduleName,
                qualifiedName = moduleName,
                isAbstract = false, // We can't determine this from the binding graph
                includes = emptyList(), // Would need additional analysis
                installedInComponents = moduleToComponents[moduleName]?.toList() ?: emptyList(),
                bindingCount = bindingCount
            )
        }
    }

    /**
     * Get all tracked modules.
     */
    fun getTrackedModules(): Set<String> = moduleBindingCounts.keys

    /**
     * Map a Dagger MissingBinding to our MissingBindingNode.
     */
    fun mapMissingBinding(missingBinding: BindingGraph.MissingBinding): MissingBindingNode {
        val key = missingBinding.key().toString()
        val id = nextNodeId()
        keyToNodeId[key] = id

        return MissingBindingNode(
            id = id,
            label = "[MISSING] $key",
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
}
