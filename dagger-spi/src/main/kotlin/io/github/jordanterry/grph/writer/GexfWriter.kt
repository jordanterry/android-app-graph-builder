package io.github.jordanterry.grph.writer

import io.github.jordanterry.grph.model.*
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

/**
 * Writes graphs in GEXF 1.3 format.
 *
 * GEXF (Graph Exchange XML Format) is a language for describing complex
 * network structures, their associated data, and dynamics.
 *
 * @see <a href="https://gexf.net/">GEXF File Format</a>
 */
class GexfWriter(
    private val prettyPrint: Boolean = true
) : GraphWriter {

    override val formatName: String = "GEXF"
    override val fileExtension: String = "gexf"

    companion object {
        private const val GEXF_NAMESPACE = "http://gexf.net/1.3"
        private const val GEXF_VERSION = "1.3"
    }

    override fun write(graph: Graph, output: OutputStream) {
        val writer = OutputStreamWriter(output, Charsets.UTF_8)
        if (prettyPrint) {
            writePretty(graph, writer)
        } else {
            writeCompact(graph, writer)
        }
        writer.flush()
    }

    private fun writePretty(graph: Graph, writer: Writer) {
        val indent = "  "
        writer.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        writer.appendLine("""<gexf xmlns="$GEXF_NAMESPACE" version="$GEXF_VERSION">""")

        // Meta
        writer.appendLine("$indent<meta>")
        writer.appendLine("$indent$indent<creator>${escapeXml(graph.metadata.creator)}</creator>")
        if (graph.metadata.description.isNotEmpty()) {
            writer.appendLine("$indent$indent<description>${escapeXml(graph.metadata.description)}</description>")
        }
        writer.appendLine("$indent$indent<lastmodifieddate>${graph.metadata.createdAt.substringBefore('T')}</lastmodifieddate>")
        writer.appendLine("$indent</meta>")

        // Graph
        val mode = if (graph.directed) "directed" else "undirected"
        writer.appendLine("$indent<graph mode=\"static\" defaultedgetype=\"$mode\">")

        // Attribute definitions for nodes
        if (graph.nodes.isNotEmpty()) {
            writer.appendLine("$indent$indent<attributes class=\"node\">")
            writer.appendLine("$indent$indent$indent<attribute id=\"nodeType\" title=\"Node Type\" type=\"string\"/>")
            writer.appendLine("$indent$indent$indent<attribute id=\"bindingKind\" title=\"Binding Kind\" type=\"string\"/>")
            writer.appendLine("$indent$indent$indent<attribute id=\"scope\" title=\"Scope\" type=\"string\"/>")
            writer.appendLine("$indent$indent$indent<attribute id=\"qualifiedName\" title=\"Qualified Name\" type=\"string\"/>")
            writer.appendLine("$indent$indent$indent<attribute id=\"isMultibinding\" title=\"Is Multibinding\" type=\"boolean\"/>")
            writer.appendLine("$indent$indent</attributes>")
        }

        // Attribute definitions for edges
        if (graph.edges.isNotEmpty()) {
            writer.appendLine("$indent$indent<attributes class=\"edge\">")
            writer.appendLine("$indent$indent$indent<attribute id=\"edgeType\" title=\"Edge Type\" type=\"string\"/>")
            writer.appendLine("$indent$indent$indent<attribute id=\"isEntryPoint\" title=\"Is Entry Point\" type=\"boolean\"/>")
            writer.appendLine("$indent$indent</attributes>")
        }

        // Nodes
        writer.appendLine("$indent$indent<nodes>")
        for (node in graph.nodes) {
            writeNode(node, writer, "$indent$indent$indent")
        }
        writer.appendLine("$indent$indent</nodes>")

        // Edges
        writer.appendLine("$indent$indent<edges>")
        for (edge in graph.edges) {
            writeEdge(edge, writer, "$indent$indent$indent")
        }
        writer.appendLine("$indent$indent</edges>")

        writer.appendLine("$indent</graph>")
        writer.appendLine("</gexf>")
    }

    private fun writeCompact(graph: Graph, writer: Writer) {
        val factory = XMLOutputFactory.newInstance()
        val xml = factory.createXMLStreamWriter(writer)

        xml.writeStartDocument("UTF-8", "1.0")
        xml.writeStartElement("gexf")
        xml.writeDefaultNamespace(GEXF_NAMESPACE)
        xml.writeAttribute("version", GEXF_VERSION)

        // Meta
        xml.writeStartElement("meta")
        xml.writeStartElement("creator")
        xml.writeCharacters(graph.metadata.creator)
        xml.writeEndElement()
        if (graph.metadata.description.isNotEmpty()) {
            xml.writeStartElement("description")
            xml.writeCharacters(graph.metadata.description)
            xml.writeEndElement()
        }
        xml.writeEndElement() // meta

        // Graph
        xml.writeStartElement("graph")
        xml.writeAttribute("mode", "static")
        xml.writeAttribute("defaultedgetype", if (graph.directed) "directed" else "undirected")

        // Nodes
        xml.writeStartElement("nodes")
        for (node in graph.nodes) {
            writeNodeXml(node, xml)
        }
        xml.writeEndElement() // nodes

        // Edges
        xml.writeStartElement("edges")
        for (edge in graph.edges) {
            writeEdgeXml(edge, xml)
        }
        xml.writeEndElement() // edges

        xml.writeEndElement() // graph
        xml.writeEndElement() // gexf
        xml.writeEndDocument()
    }

    private fun writeNode(node: Node, writer: Writer, indent: String) {
        writer.append("$indent<node id=\"${escapeXml(node.id)}\" label=\"${escapeXml(node.label)}\">")
        writer.appendLine()
        writer.appendLine("$indent  <attvalues>")
        writer.appendLine("$indent    <attvalue for=\"nodeType\" value=\"${node.type.name}\"/>")

        when (node) {
            is BindingNode -> {
                writer.appendLine("$indent    <attvalue for=\"bindingKind\" value=\"${node.bindingKind.name}\"/>")
                node.scope?.let {
                    writer.appendLine("$indent    <attvalue for=\"scope\" value=\"${escapeXml(it)}\"/>")
                }
                writer.appendLine("$indent    <attvalue for=\"qualifiedName\" value=\"${escapeXml(node.key)}\"/>")
                writer.appendLine("$indent    <attvalue for=\"isMultibinding\" value=\"${node.isMultibinding}\"/>")
            }
            is ComponentNode -> {
                writer.appendLine("$indent    <attvalue for=\"qualifiedName\" value=\"${escapeXml(node.qualifiedName)}\"/>")
                if (node.scopes.isNotEmpty()) {
                    writer.appendLine("$indent    <attvalue for=\"scope\" value=\"${escapeXml(node.scopes.joinToString(","))}\"/>")
                }
            }
            is ModuleNode -> {
                writer.appendLine("$indent    <attvalue for=\"qualifiedName\" value=\"${escapeXml(node.qualifiedName)}\"/>")
            }
            is MissingBindingNode -> {
                writer.appendLine("$indent    <attvalue for=\"qualifiedName\" value=\"${escapeXml(node.key)}\"/>")
            }
        }

        // Custom attributes
        for ((key, value) in node.attributes) {
            writer.appendLine("$indent    <attvalue for=\"${escapeXml(key)}\" value=\"${escapeXml(value)}\"/>")
        }

        writer.appendLine("$indent  </attvalues>")
        writer.appendLine("$indent</node>")
    }

    private fun writeNodeXml(node: Node, xml: XMLStreamWriter) {
        xml.writeStartElement("node")
        xml.writeAttribute("id", node.id)
        xml.writeAttribute("label", node.label)

        xml.writeStartElement("attvalues")
        writeAttValue(xml, "nodeType", node.type.name)

        when (node) {
            is BindingNode -> {
                writeAttValue(xml, "bindingKind", node.bindingKind.name)
                node.scope?.let { writeAttValue(xml, "scope", it) }
                writeAttValue(xml, "qualifiedName", node.key)
                writeAttValue(xml, "isMultibinding", node.isMultibinding.toString())
            }
            is ComponentNode -> {
                writeAttValue(xml, "qualifiedName", node.qualifiedName)
                if (node.scopes.isNotEmpty()) {
                    writeAttValue(xml, "scope", node.scopes.joinToString(","))
                }
            }
            is ModuleNode -> {
                writeAttValue(xml, "qualifiedName", node.qualifiedName)
            }
            is MissingBindingNode -> {
                writeAttValue(xml, "qualifiedName", node.key)
            }
        }

        for ((key, value) in node.attributes) {
            writeAttValue(xml, key, value)
        }

        xml.writeEndElement() // attvalues
        xml.writeEndElement() // node
    }

    private fun writeEdge(edge: Edge, writer: Writer, indent: String) {
        writer.append("$indent<edge id=\"${escapeXml(edge.id)}\" source=\"${escapeXml(edge.source)}\" target=\"${escapeXml(edge.target)}\">")
        writer.appendLine()
        writer.appendLine("$indent  <attvalues>")
        writer.appendLine("$indent    <attvalue for=\"edgeType\" value=\"${edge.type.name}\"/>")

        if (edge is DependencyEdge) {
            writer.appendLine("$indent    <attvalue for=\"isEntryPoint\" value=\"${edge.isEntryPoint}\"/>")
        }

        for ((key, value) in edge.attributes) {
            writer.appendLine("$indent    <attvalue for=\"${escapeXml(key)}\" value=\"${escapeXml(value)}\"/>")
        }

        writer.appendLine("$indent  </attvalues>")
        writer.appendLine("$indent</edge>")
    }

    private fun writeEdgeXml(edge: Edge, xml: XMLStreamWriter) {
        xml.writeStartElement("edge")
        xml.writeAttribute("id", edge.id)
        xml.writeAttribute("source", edge.source)
        xml.writeAttribute("target", edge.target)

        xml.writeStartElement("attvalues")
        writeAttValue(xml, "edgeType", edge.type.name)

        if (edge is DependencyEdge) {
            writeAttValue(xml, "isEntryPoint", edge.isEntryPoint.toString())
        }

        for ((key, value) in edge.attributes) {
            writeAttValue(xml, key, value)
        }

        xml.writeEndElement() // attvalues
        xml.writeEndElement() // edge
    }

    private fun writeAttValue(xml: XMLStreamWriter, forAttr: String, value: String) {
        xml.writeEmptyElement("attvalue")
        xml.writeAttribute("for", forAttr)
        xml.writeAttribute("value", value)
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
