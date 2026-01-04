package io.github.jordanterry.appgraph.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EdgeTest {

    @Test
    fun `DependencyEdge connects source to target`() {
        val edge = DependencyEdge(
            id = "e1",
            source = "n1",
            target = "n2",
            isEntryPoint = false
        )

        assertThat(edge.source).isEqualTo("n1")
        assertThat(edge.target).isEqualTo("n2")
        assertThat(edge.type).isEqualTo(EdgeType.DEPENDENCY)
    }

    @Test
    fun `DependencyEdge entry point flag is preserved`() {
        val entryPointEdge = DependencyEdge(
            id = "e1",
            source = "component",
            target = "binding",
            isEntryPoint = true
        )

        assertThat(entryPointEdge.isEntryPoint).isTrue()
    }

    @Test
    fun `ComponentHierarchyEdge has correct type`() {
        val edge = ComponentHierarchyEdge(
            id = "e1",
            source = "parent",
            target = "child"
        )

        assertThat(edge.type).isEqualTo(EdgeType.COMPONENT_HIERARCHY)
    }

    @Test
    fun `ModuleInclusionEdge has correct type`() {
        val edge = ModuleInclusionEdge(
            id = "e1",
            source = "component",
            target = "module"
        )

        assertThat(edge.type).isEqualTo(EdgeType.MODULE_INCLUSION)
    }

    @Test
    fun `edge attributes are accessible`() {
        val edge = DependencyEdge(
            id = "e1",
            source = "n1",
            target = "n2",
            isEntryPoint = false,
            attributes = mapOf("weight" to "1.0")
        )

        assertThat(edge.attributes).containsEntry("weight", "1.0")
    }
}
