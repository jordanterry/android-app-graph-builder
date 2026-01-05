package io.github.jordanterry.appgraph.source.metro

import com.google.common.truth.Truth.assertThat
import io.github.jordanterry.appgraph.model.BindingKind
import io.github.jordanterry.appgraph.model.BindingNode
import io.github.jordanterry.appgraph.model.ComponentNode
import org.junit.jupiter.api.Test

class MetroGraphExtractorTest {

    private val extractor = MetroGraphExtractor()

    private val sampleMetroJson = """
        {
          "graph": "sample.AppGraph",
          "scopes": [],
          "aggregationScopes": [],
          "roots": {
            "accessors": [
              {
                "key": "sample.UserService",
                "isDeferrable": false
              },
              {
                "key": "sample.AnalyticsService",
                "isDeferrable": false
              }
            ],
            "injectors": []
          },
          "extensions": {
            "accessors": [],
            "factoryAccessors": [],
            "factoriesImplemented": []
          },
          "bindings": [
            {
              "key": "sample.AnalyticsService",
              "bindingKind": "ConstructorInjected",
              "isScoped": false,
              "nameHint": "AnalyticsService",
              "dependencies": [],
              "isSynthetic": false,
              "origin": "AppGraph.kt:36:1",
              "declaration": "AnalyticsService",
              "multibinding": null,
              "optionalWrapper": null
            },
            {
              "key": "sample.AppGraph",
              "bindingKind": "BoundInstance",
              "isScoped": false,
              "nameHint": "AppGraphProvider",
              "dependencies": [],
              "isSynthetic": false,
              "origin": "AppGraph.kt:11:1",
              "declaration": "AppGraph",
              "multibinding": null,
              "optionalWrapper": null
            },
            {
              "key": "sample.UserRepository",
              "bindingKind": "ConstructorInjected",
              "isScoped": false,
              "nameHint": "UserRepository",
              "dependencies": [],
              "isSynthetic": false,
              "origin": "AppGraph.kt:23:1",
              "declaration": "UserRepository",
              "multibinding": null,
              "optionalWrapper": null
            },
            {
              "key": "sample.UserService",
              "bindingKind": "ConstructorInjected",
              "isScoped": false,
              "nameHint": "UserService",
              "dependencies": [
                {
                  "key": "sample.UserRepository",
                  "hasDefault": false,
                  "isAssisted": false
                }
              ],
              "isSynthetic": false,
              "origin": "AppGraph.kt:29:1",
              "declaration": "UserService",
              "multibinding": null,
              "optionalWrapper": null
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `extracts graph name correctly`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        assertThat(graph.name).isEqualTo("sample.AppGraph")
        assertThat(graph.id).isEqualTo("sample_AppGraph")
    }

    @Test
    fun `extracts component node for graph`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        val componentNodes = graph.nodes.filterIsInstance<ComponentNode>()
        assertThat(componentNodes).hasSize(1)

        val component = componentNodes.first()
        assertThat(component.label).isEqualTo("AppGraph")
        assertThat(component.qualifiedName).isEqualTo("sample.AppGraph")
        assertThat(component.isSubcomponent).isFalse()
    }

    @Test
    fun `extracts binding nodes`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        val bindingNodes = graph.nodes.filterIsInstance<BindingNode>()
        assertThat(bindingNodes).hasSize(4) // AnalyticsService, AppGraph, UserRepository, UserService
    }

    @Test
    fun `maps binding kinds correctly`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        val bindingNodes = graph.nodes.filterIsInstance<BindingNode>()
        val bindingsByKey = bindingNodes.associateBy { it.key }

        assertThat(bindingsByKey["sample.AnalyticsService"]?.bindingKind)
            .isEqualTo(BindingKind.INJECTION)
        assertThat(bindingsByKey["sample.AppGraph"]?.bindingKind)
            .isEqualTo(BindingKind.BOUND_INSTANCE)
        assertThat(bindingsByKey["sample.UserService"]?.bindingKind)
            .isEqualTo(BindingKind.INJECTION)
    }

    @Test
    fun `extracts dependency edges`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        // UserService depends on UserRepository
        val dependencyEdges = graph.edges.filter { !it.attributes.containsKey("type") && it.attributes["deferrable"] != "true" }
            .filter { edge ->
                // Find edges that are not entry points (from graph to accessor)
                val sourceNode = graph.nodes.find { it.id == edge.source }
                sourceNode !is ComponentNode
            }

        assertThat(dependencyEdges).hasSize(1)
    }

    @Test
    fun `extracts entry point edges from accessors`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        // Should have entry point edges from AppGraph to UserService and AnalyticsService
        val componentNode = graph.nodes.filterIsInstance<ComponentNode>().first()
        val entryPointEdges = graph.edges.filter { it.source == componentNode.id }

        assertThat(entryPointEdges).hasSize(2)
    }

    @Test
    fun `sets metadata correctly`() {
        val graph = extractor.extractFromJson(sampleMetroJson)

        assertThat(graph.metadata.creator).isEqualTo("appgraph-metro-source")
        assertThat(graph.metadata.description).contains("sample.AppGraph")
    }
}
