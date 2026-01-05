package io.github.jordanterry.appgraph.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class NodeTest {

    @Test
    fun `ComponentNode has correct type`() {
        val node = ComponentNode(
            id = "c1",
            label = "AppComponent",
            qualifiedName = "com.example.AppComponent",
            isSubcomponent = false,
            scopes = listOf("@Singleton"),
            componentPath = "com.example.AppComponent"
        )

        assertThat(node.type).isEqualTo(NodeType.COMPONENT)
        assertThat(node.isSubcomponent).isFalse()
    }

    @Test
    fun `BindingNode captures binding kind`() {
        val node = BindingNode(
            id = "b1",
            label = "UserRepository",
            key = "com.example.UserRepository",
            bindingKind = BindingKind.INJECTION,
            scope = "@Singleton",
            contributingModule = null,
            isMultibinding = false
        )

        assertThat(node.type).isEqualTo(NodeType.BINDING)
        assertThat(node.bindingKind).isEqualTo(BindingKind.INJECTION)
        assertThat(node.scope).isEqualTo("@Singleton")
    }

    @Test
    fun `ModuleNode captures includes list`() {
        val node = ModuleNode(
            id = "m1",
            label = "AppModule",
            qualifiedName = "com.example.AppModule",
            isAbstract = false,
            includes = listOf("com.example.NetworkModule", "com.example.DatabaseModule")
        )

        assertThat(node.type).isEqualTo(NodeType.MODULE)
        assertThat(node.includes).hasSize(2)
        assertThat(node.includes).contains("com.example.NetworkModule")
    }

    @Test
    fun `MissingBindingNode has correct type`() {
        val node = MissingBindingNode(
            id = "missing1",
            label = "[MISSING] String",
            key = "java.lang.String"
        )

        assertThat(node.type).isEqualTo(NodeType.MISSING_BINDING)
    }

    @Test
    fun `node attributes are accessible`() {
        val node = BindingNode(
            id = "b1",
            label = "Test",
            key = "com.example.Test",
            bindingKind = BindingKind.PROVISION,
            scope = null,
            contributingModule = "com.example.TestModule",
            isMultibinding = false,
            attributes = mapOf("custom" to "value")
        )

        assertThat(node.attributes).containsEntry("custom", "value")
    }

    @Test
    fun `multibinding is correctly identified`() {
        val setBinding = BindingNode(
            id = "b1",
            label = "Set<String>",
            key = "java.util.Set<java.lang.String>",
            bindingKind = BindingKind.MULTIBOUND_SET,
            scope = null,
            contributingModule = null,
            isMultibinding = true
        )

        assertThat(setBinding.isMultibinding).isTrue()
        assertThat(setBinding.bindingKind).isEqualTo(BindingKind.MULTIBOUND_SET)
    }

    @Test
    fun `BindingNode captures componentPath and isEntryPoint`() {
        val node = BindingNode(
            id = "b1",
            label = "UserRepository",
            key = "com.example.UserRepository",
            bindingKind = BindingKind.INJECTION,
            scope = "@Singleton",
            contributingModule = "com.example.AppModule",
            isMultibinding = false,
            componentPath = "com.example.AppComponent",
            isEntryPoint = true
        )

        assertThat(node.componentPath).isEqualTo("com.example.AppComponent")
        assertThat(node.isEntryPoint).isTrue()
        assertThat(node.contributingModule).isEqualTo("com.example.AppModule")
    }

    @Test
    fun `ModuleNode captures installedInComponents and bindingCount`() {
        val node = ModuleNode(
            id = "m1",
            label = "AppModule",
            qualifiedName = "com.example.AppModule",
            isAbstract = false,
            includes = emptyList(),
            installedInComponents = listOf("com.example.AppComponent", "com.example.ActivityComponent"),
            bindingCount = 5
        )

        assertThat(node.installedInComponents).hasSize(2)
        assertThat(node.installedInComponents).contains("com.example.AppComponent")
        assertThat(node.bindingCount).isEqualTo(5)
    }
}
