package io.github.jordanterry.grph.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GraphTest {

    @Test
    fun `graph contains nodes and edges`() {
        val node = BindingNode(
            id = "n1",
            label = "UserRepository",
            key = "com.example.UserRepository",
            bindingKind = BindingKind.INJECTION,
            scope = null,
            contributingModule = null,
            isMultibinding = false
        )

        val edge = DependencyEdge(
            id = "e1",
            source = "n1",
            target = "n2",
            isEntryPoint = false
        )

        val graph = Graph(
            id = "test_graph",
            name = "TestGraph",
            nodes = listOf(node),
            edges = listOf(edge)
        )

        assertThat(graph.nodes).hasSize(1)
        assertThat(graph.edges).hasSize(1)
        assertThat(graph.id).isEqualTo("test_graph")
        assertThat(graph.name).isEqualTo("TestGraph")
    }

    @Test
    fun `graph with empty nodes is valid`() {
        val graph = Graph(
            id = "empty",
            name = "EmptyGraph",
            nodes = emptyList(),
            edges = emptyList()
        )

        assertThat(graph.nodes).isEmpty()
        assertThat(graph.edges).isEmpty()
    }

    @Test
    fun `graph metadata is preserved`() {
        val metadata = GraphMetadata(
            creator = "test-creator",
            description = "Test description"
        )

        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = emptyList(),
            edges = emptyList(),
            metadata = metadata
        )

        assertThat(graph.metadata.creator).isEqualTo("test-creator")
        assertThat(graph.metadata.description).isEqualTo("Test description")
    }

    @Test
    fun `graph is directed by default`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = emptyList(),
            edges = emptyList()
        )

        assertThat(graph.directed).isTrue()
    }
}
