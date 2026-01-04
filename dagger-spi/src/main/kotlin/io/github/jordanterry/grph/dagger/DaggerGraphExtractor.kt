package io.github.jordanterry.grph.dagger

import dagger.spi.model.BindingGraph
import io.github.jordanterry.grph.model.*

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

        // Extract component nodes
        bindingGraph.componentNodes().forEach { componentNode ->
            nodes.add(mapper.mapComponentNode(componentNode))
        }

        // Extract binding nodes
        bindingGraph.bindings().forEach { binding ->
            nodes.add(mapper.mapBindingNode(binding))
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

        val rootComponent = bindingGraph.rootComponentNode()
        val graphName = rootComponent.componentPath().toString()

        return Graph(
            id = graphName.replace(".", "_").replace("[", "").replace("]", ""),
            name = graphName,
            nodes = nodes,
            edges = edges,
            metadata = GraphMetadata(
                creator = "grph-dagger-spi",
                description = "Dagger dependency graph for $graphName"
            )
        )
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
