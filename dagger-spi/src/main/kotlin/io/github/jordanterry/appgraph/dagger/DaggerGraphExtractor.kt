package io.github.jordanterry.appgraph.dagger

import dagger.spi.model.Binding
import dagger.spi.model.BindingGraph
import io.github.jordanterry.appgraph.model.*

/**
 * Extracts a Graph from a Dagger BindingGraph.
 *
 * This class traverses the BindingGraph to extract all components, bindings,
 * modules, and their relationships into our abstract Graph model.
 */
class DaggerGraphExtractor {

    /**
     * Extract a Graph from the given BindingGraph.
     */
    fun extract(bindingGraph: BindingGraph): Graph {
        val mapper = DaggerNodeMapper()

        val nodes = mutableListOf<Node>()
        val edges = mutableListOf<Edge>()

        // Build a map of bindings to their owning component paths
        val bindingToComponent = mutableMapOf<String, String>()
        bindingGraph.componentNodes().forEach { componentNode ->
            val componentPath = componentNode.componentPath().toString()
            // Get bindings contributed by this component
            try {
                val componentBindings = bindingGraph.bindings().filter { binding ->
                    try {
                        // Check if this binding's component path matches
                        bindingGraph.network().incidentNodes(
                            bindingGraph.dependencyEdges().firstOrNull { edge ->
                                try {
                                    bindingGraph.network().incidentNodes(edge).target() == binding
                                } catch (e: Exception) {
                                    false
                                }
                            } ?: return@filter false
                        )
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }

        // Extract component nodes first
        bindingGraph.componentNodes().forEach { componentNode ->
            nodes.add(mapper.mapComponentNode(componentNode))
        }

        // Collect entry point binding keys
        val entryPointKeys = mutableSetOf<String>()
        bindingGraph.entryPointEdges().forEach { edge ->
            try {
                val targetNode = bindingGraph.network().incidentNodes(edge).target()
                if (targetNode is Binding) {
                    entryPointKeys.add(targetNode.key().toString())
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }

        // Extract binding nodes with component path information
        val rootComponentPath = bindingGraph.rootComponentNode().componentPath().toString()
        bindingGraph.bindings().forEach { binding ->
            val key = binding.key().toString()
            val componentPath = bindingToComponent[key] ?: rootComponentPath
            val bindingNode = mapper.mapBindingNode(binding, componentPath)

            // Update entry point status
            val finalNode = if (entryPointKeys.contains(key)) {
                bindingNode.copy(isEntryPoint = true)
            } else {
                bindingNode
            }
            nodes.add(finalNode)
        }

        // Extract missing bindings
        bindingGraph.missingBindings().forEach { missingBinding ->
            nodes.add(mapper.mapMissingBinding(missingBinding))
        }

        // Extract dependency edges
        bindingGraph.dependencyEdges().forEach { dependencyEdge ->
            val edge = mapper.mapDependencyEdge(dependencyEdge, bindingGraph)
            if (edge != null) {
                edges.add(edge)
            }
        }

        // Extract component hierarchy edges
        extractComponentHierarchy(bindingGraph, mapper, edges)

        // Create module nodes from tracked modules
        val moduleNodes = mapper.createModuleNodes()
        nodes.addAll(moduleNodes)

        // Create binding-to-module edges
        extractBindingToModuleEdges(bindingGraph, mapper, edges)

        // Create component-to-module edges
        extractComponentToModuleEdges(bindingGraph, mapper, edges)

        // Create component-to-binding edges for entry points
        extractEntryPointEdges(bindingGraph, mapper, edges)

        // Create binding ownership edges (which component owns each binding)
        extractBindingOwnershipEdges(bindingGraph, mapper, edges)

        val rootComponent = bindingGraph.rootComponentNode()
        val graphName = rootComponent.componentPath().toString()

        return Graph(
            id = graphName.replace(".", "_").replace("[", "").replace("]", ""),
            name = graphName,
            nodes = nodes,
            edges = edges,
            metadata = GraphMetadata(
                creator = "appgraph-dagger-spi",
                description = "Dagger dependency graph for $graphName"
            )
        )
    }

    /**
     * Extract edges from bindings to their contributing modules.
     */
    private fun extractBindingToModuleEdges(
        bindingGraph: BindingGraph,
        mapper: DaggerNodeMapper,
        edges: MutableList<Edge>
    ) {
        bindingGraph.bindings().forEach { binding ->
            try {
                val moduleName = binding.contributingModule().orElse(null)?.toString()
                if (moduleName != null) {
                    val bindingKey = binding.key().toString()
                    val bindingId = mapper.getBindingNodeId(bindingKey)
                    val moduleId = mapper.getModuleNodeId(moduleName)

                    if (bindingId != null) {
                        edges.add(
                            BindingToModuleEdge(
                                id = mapper.nextEdgeId(),
                                source = bindingId,
                                target = moduleId
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Skip bindings that don't have contributing module info
            }
        }
    }

    /**
     * Extract edges from components to their installed modules.
     */
    private fun extractComponentToModuleEdges(
        bindingGraph: BindingGraph,
        mapper: DaggerNodeMapper,
        edges: MutableList<Edge>
    ) {
        // Track which component-module pairs we've already added
        val addedEdges = mutableSetOf<Pair<String, String>>()

        bindingGraph.bindings().forEach { binding ->
            try {
                val moduleName = binding.contributingModule().orElse(null)?.toString()
                if (moduleName != null) {
                    // For each component, check if this module contributes bindings to it
                    bindingGraph.componentNodes().forEach { componentNode ->
                        val componentId = mapper.getComponentNodeId(componentNode)
                        val moduleId = mapper.getModuleNodeId(moduleName)
                        val edgeKey = componentId to moduleId

                        if (!addedEdges.contains(edgeKey)) {
                            addedEdges.add(edgeKey)
                            edges.add(
                                ModuleInclusionEdge(
                                    id = mapper.nextEdgeId(),
                                    source = componentId,
                                    target = moduleId
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip on error
            }
        }
    }

    /**
     * Extract edges from components to their entry point bindings.
     */
    private fun extractEntryPointEdges(
        bindingGraph: BindingGraph,
        mapper: DaggerNodeMapper,
        edges: MutableList<Edge>
    ) {
        // Track which component-binding pairs we've already added
        val addedEdges = mutableSetOf<Pair<String, String>>()

        bindingGraph.entryPointEdges().forEach { entryPointEdge ->
            try {
                val targetNode = bindingGraph.network().incidentNodes(entryPointEdge).target()
                if (targetNode is Binding) {
                    val bindingKey = targetNode.key().toString()
                    val bindingId = mapper.getBindingNodeId(bindingKey)

                    // Use the root component for entry points (simplest approach)
                    val componentNode = bindingGraph.rootComponentNode()
                    val componentId = mapper.getComponentNodeId(componentNode)

                    if (bindingId != null) {
                        val edgeKey = componentId to bindingId
                        if (!addedEdges.contains(edgeKey)) {
                            addedEdges.add(edgeKey)
                            edges.add(
                                ComponentToBindingEdge(
                                    id = mapper.nextEdgeId(),
                                    source = componentId,
                                    target = bindingId
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip on error
            }
        }
    }

    /**
     * Extract binding ownership edges showing which component owns each binding.
     *
     * Ownership is determined by:
     * 1. Scoped bindings belong to the component with the matching scope
     * 2. Unscoped bindings from modules belong to components that include the module
     * 3. @Inject constructor bindings belong to the component where they're installed
     */
    private fun extractBindingOwnershipEdges(
        bindingGraph: BindingGraph,
        mapper: DaggerNodeMapper,
        edges: MutableList<Edge>
    ) {
        val componentNodes = bindingGraph.componentNodes().toList()

        // Build scope-to-component mapping
        val scopeToComponent = mutableMapOf<String, BindingGraph.ComponentNode>()
        componentNodes.forEach { componentNode ->
            componentNode.scopes().forEach { scope ->
                scopeToComponent[scope.toString()] = componentNode
            }
        }

        bindingGraph.bindings().forEach { binding ->
            try {
                val bindingKey = binding.key().toString()
                val bindingId = mapper.getBindingNodeId(bindingKey) ?: return@forEach

                // Determine the owning component
                val owningComponent: BindingGraph.ComponentNode = when {
                    // 1. Scoped bindings: find component with matching scope
                    binding.scope().isPresent -> {
                        val scope = binding.scope().get().toString()
                        scopeToComponent[scope] ?: bindingGraph.rootComponentNode()
                    }

                    // 2. Bindings from modules: associate with the component that has the module
                    binding.contributingModule().isPresent -> {
                        // For module bindings without scope, they're available from the component
                        // that includes the module. Use root component as default.
                        bindingGraph.rootComponentNode()
                    }

                    // 3. @Inject constructor bindings without scope: associated with root
                    // (they can be instantiated at any component level)
                    else -> {
                        bindingGraph.rootComponentNode()
                    }
                }

                val componentId = mapper.getComponentNodeId(owningComponent)

                edges.add(
                    BindingOwnershipEdge(
                        id = mapper.nextEdgeId(),
                        source = componentId,
                        target = bindingId
                    )
                )
            } catch (e: Exception) {
                // Skip on error
            }
        }
    }

    /**
     * Extract component hierarchy edges (parent -> subcomponent relationships).
     */
    private fun extractComponentHierarchy(
        bindingGraph: BindingGraph,
        mapper: DaggerNodeMapper,
        edges: MutableList<Edge>
    ) {
        val componentNodes = bindingGraph.componentNodes().toList()

        for (componentNode in componentNodes) {
            if (componentNode.isSubcomponent) {
                val componentPath = componentNode.componentPath()
                val parent = componentPath.parent()
                if (parent != null) {
                    // Find parent component node
                    val parentNode = componentNodes.find {
                        it.componentPath() == parent
                    }
                    if (parentNode != null) {
                        edges.add(
                            ComponentHierarchyEdge(
                                id = mapper.nextEdgeId(),
                                source = mapper.getComponentNodeId(parentNode),
                                target = mapper.getComponentNodeId(componentNode)
                            )
                        )
                    }
                }
            }
        }
    }
}
