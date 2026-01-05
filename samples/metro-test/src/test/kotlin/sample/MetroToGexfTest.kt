package sample

import io.github.jordanterry.appgraph.source.MetroGraphSourceInput
import io.github.jordanterry.appgraph.source.GraphSourceResult
import io.github.jordanterry.appgraph.source.metro.MetroGraphSource
import io.github.jordanterry.appgraph.writer.gexf.GexfWriter
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration test that verifies the full pipeline:
 * Metro JSON metadata -> Graph model -> GEXF output
 */
class MetroToGexfTest {

    @Test
    fun `converts Metro graph metadata to GEXF`() {
        // Path to the generated Metro metadata
        val metadataDir = Paths.get("build/reports/metro/main/graph-metadata")

        if (!metadataDir.toFile().exists()) {
            println("Metro metadata not found. Run './gradlew :samples:metro-test:compileKotlin' first.")
            return
        }

        // Create Metro source and extract graphs
        val source = MetroGraphSource()
        val input = MetroGraphSourceInput(paths = listOf(metadataDir))

        when (val result = source.extract(input)) {
            is GraphSourceResult.Success -> {
                assertTrue(result.graphs.isNotEmpty(), "Should extract at least one graph")

                val graph = result.graphs.first()
                println("Extracted graph: ${graph.name}")
                println("  Nodes: ${graph.nodes.size}")
                println("  Edges: ${graph.edges.size}")

                // Convert to GEXF
                val writer = GexfWriter(prettyPrint = true)
                val outputStream = ByteArrayOutputStream()
                writer.write(graph, outputStream)

                val gexfOutput = outputStream.toString()
                assertTrue(gexfOutput.contains("<?xml"), "Should produce valid XML")
                assertTrue(gexfOutput.contains("gexf"), "Should produce GEXF format")
                assertTrue(gexfOutput.contains("AppGraph"), "Should contain graph name")

                println("\nGEXF Output (first 1000 chars):")
                println(gexfOutput.take(1000))
            }
            is GraphSourceResult.Error -> {
                fail("Failed to extract graph: ${result.message}")
            }
            is GraphSourceResult.Partial -> {
                println("Partial extraction with errors: ${result.errors}")
                assertTrue(result.graphs.isNotEmpty(), "Should have some graphs even with errors")
            }
        }
    }
}
