package io.github.jordanterry.appgraph.writer.gexf

import com.google.common.truth.Truth.assertThat
import io.github.jordanterry.appgraph.model.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory

class GexfWriterTest {

    private val writer = GexfWriter(prettyPrint = true)

    @Test
    fun `writes valid XML declaration`() {
        val graph = createSimpleGraph()
        val output = writeToString(graph)

        assertThat(output).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    @Test
    fun `includes gexf namespace and version`() {
        val graph = createSimpleGraph()
        val output = writeToString(graph)

        assertThat(output).contains("xmlns=\"http://gexf.net/1.3\"")
        assertThat(output).contains("version=\"1.3\"")
    }

    @Test
    fun `writes graph element with correct mode`() {
        val graph = createSimpleGraph()
        val output = writeToString(graph)

        assertThat(output).contains("<graph mode=\"static\" defaultedgetype=\"directed\">")
    }

    @Test
    fun `writes all nodes with id and label`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                BindingNode(
                    id = "n1",
                    label = "UserRepo",
                    key = "com.example.UserRepo",
                    bindingKind = BindingKind.INJECTION,
                    scope = null,
                    contributingModule = null,
                    isMultibinding = false
                ),
                BindingNode(
                    id = "n2",
                    label = "ApiService",
                    key = "com.example.ApiService",
                    bindingKind = BindingKind.PROVISION,
                    scope = null,
                    contributingModule = null,
                    isMultibinding = false
                )
            ),
            edges = emptyList()
        )

        val output = writeToString(graph)

        assertThat(output).contains("id=\"n1\"")
        assertThat(output).contains("label=\"UserRepo\"")
        assertThat(output).contains("id=\"n2\"")
        assertThat(output).contains("label=\"ApiService\"")
    }

    @Test
    fun `writes all edges with source and target`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                BindingNode("n1", "A", key = "A", bindingKind = BindingKind.INJECTION, scope = null, contributingModule = null, isMultibinding = false),
                BindingNode("n2", "B", key = "B", bindingKind = BindingKind.INJECTION, scope = null, contributingModule = null, isMultibinding = false)
            ),
            edges = listOf(
                DependencyEdge(id = "e1", source = "n1", target = "n2", isEntryPoint = false)
            )
        )

        val output = writeToString(graph)

        assertThat(output).contains("id=\"e1\"")
        assertThat(output).contains("source=\"n1\"")
        assertThat(output).contains("target=\"n2\"")
    }

    @Test
    fun `includes node attributes definitions`() {
        val graph = createSimpleGraph()
        val output = writeToString(graph)

        assertThat(output).contains("<attributes class=\"node\">")
        assertThat(output).contains("id=\"nodeType\"")
        assertThat(output).contains("id=\"bindingKind\"")
    }

    @Test
    fun `includes edge attributes definitions`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                BindingNode("n1", "A", key = "A", bindingKind = BindingKind.INJECTION, scope = null, contributingModule = null, isMultibinding = false),
                BindingNode("n2", "B", key = "B", bindingKind = BindingKind.INJECTION, scope = null, contributingModule = null, isMultibinding = false)
            ),
            edges = listOf(
                DependencyEdge(id = "e1", source = "n1", target = "n2", isEntryPoint = true)
            )
        )

        val output = writeToString(graph)

        assertThat(output).contains("<attributes class=\"edge\">")
        assertThat(output).contains("id=\"edgeType\"")
        assertThat(output).contains("id=\"isEntryPoint\"")
    }

    @Test
    fun `node attribute values are written correctly`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                BindingNode(
                    id = "n1",
                    label = "Repo",
                    key = "com.example.Repo",
                    bindingKind = BindingKind.PROVISION,
                    scope = "@Singleton",
                    contributingModule = "com.example.AppModule",
                    isMultibinding = false
                )
            ),
            edges = emptyList()
        )

        val output = writeToString(graph)

        assertThat(output).contains("for=\"nodeType\" value=\"BINDING\"")
        assertThat(output).contains("for=\"bindingKind\" value=\"PROVISION\"")
        assertThat(output).contains("for=\"scope\" value=\"@Singleton\"")
    }

    @Test
    fun `handles special XML characters in labels`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                BindingNode(
                    id = "n1",
                    label = "Map<String, Int>",
                    key = "java.util.Map<java.lang.String, java.lang.Integer>",
                    bindingKind = BindingKind.MULTIBOUND_MAP,
                    scope = null,
                    contributingModule = null,
                    isMultibinding = true
                )
            ),
            edges = emptyList()
        )

        val output = writeToString(graph)

        // < and > should be escaped
        assertThat(output).contains("&lt;")
        assertThat(output).contains("&gt;")
    }

    @Test
    fun `metadata creator and description included`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = emptyList(),
            edges = emptyList(),
            metadata = GraphMetadata(
                creator = "test-creator",
                description = "Test description"
            )
        )

        val output = writeToString(graph)

        assertThat(output).contains("<creator>test-creator</creator>")
        assertThat(output).contains("<description>Test description</description>")
    }

    @Test
    fun `empty graph produces valid GEXF`() {
        val graph = Graph(
            id = "empty",
            name = "Empty",
            nodes = emptyList(),
            edges = emptyList()
        )

        val output = writeToString(graph)

        assertThat(output).contains("<gexf")
        assertThat(output).contains("</gexf>")
        assertThat(output).contains("<nodes>")
        assertThat(output).contains("</nodes>")
    }

    @Test
    fun `output is well-formed XML`() {
        val graph = createSimpleGraph()
        val output = writeToString(graph)

        // Parse with DocumentBuilder - should not throw
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(output.byteInputStream())

        assertThat(document.documentElement.tagName).isEqualTo("gexf")
    }

    @Test
    fun `ComponentNode writes correctly`() {
        val graph = Graph(
            id = "test",
            name = "Test",
            nodes = listOf(
                ComponentNode(
                    id = "c1",
                    label = "AppComponent",
                    qualifiedName = "com.example.AppComponent",
                    isSubcomponent = false,
                    scopes = listOf("@Singleton"),
                    componentPath = "com.example.AppComponent"
                )
            ),
            edges = emptyList()
        )

        val output = writeToString(graph)

        assertThat(output).contains("for=\"nodeType\" value=\"COMPONENT\"")
        assertThat(output).contains("for=\"qualifiedName\" value=\"com.example.AppComponent\"")
    }

    private fun createSimpleGraph(): Graph {
        return Graph(
            id = "simple",
            name = "SimpleGraph",
            nodes = listOf(
                BindingNode(
                    id = "n1",
                    label = "Test",
                    key = "com.example.Test",
                    bindingKind = BindingKind.INJECTION,
                    scope = null,
                    contributingModule = null,
                    isMultibinding = false
                )
            ),
            edges = emptyList()
        )
    }

    private fun writeToString(graph: Graph): String {
        val output = ByteArrayOutputStream()
        writer.write(graph, output)
        return output.toString(Charsets.UTF_8)
    }
}
