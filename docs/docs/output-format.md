---
sidebar_position: 4
---

# Output Format

AppGraph outputs graphs in [GEXF (Graph Exchange XML Format)](https://gexf.net/) version 1.3. This format is widely supported by graph visualization and analysis tools.

## GEXF Overview

GEXF is an XML-based format designed for representing complex networks. It supports:

- Directed and undirected graphs
- Node and edge attributes
- Hierarchical structures
- Metadata

## Graph Structure

Each Dagger component generates a separate GEXF file containing:

### Nodes

Nodes represent entities in your dependency graph:

| Node Type | Description |
|-----------|-------------|
| `COMPONENT` | Dagger `@Component` or `@Subcomponent` |
| `BINDING` | A binding (from `@Inject`, `@Provides`, `@Binds`, etc.) |
| `MODULE` | A Dagger `@Module` class |
| `MISSING_BINDING` | An unresolved dependency |

### Edges

Edges represent relationships between nodes:

| Edge Type | Description |
|-----------|-------------|
| `DEPENDENCY` | A binding depends on another binding |
| `COMPONENT_HIERARCHY` | Parent-child component relationship |
| `MODULE_INCLUSION` | Module includes another module |

## Node Attributes

Each node includes attributes providing detailed information:

### Common Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `nodeType` | String | Type of node (COMPONENT, BINDING, MODULE, MISSING_BINDING) |
| `qualifiedName` | String | Fully qualified class/key name |

### Binding Node Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `bindingKind` | String | How the binding is provided |
| `scope` | String | Scope annotation (e.g., `@Singleton`) |
| `isMultibinding` | Boolean | Whether this is a multibinding contribution |

### Component Node Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `isSubcomponent` | Boolean | Whether this is a subcomponent |
| `scopes` | String | Comma-separated list of scopes |

## Binding Kinds

The `bindingKind` attribute indicates how a binding is provided:

| Kind | Description |
|------|-------------|
| `INJECTION` | `@Inject`-annotated constructor |
| `PROVISION` | `@Provides` method in a module |
| `DELEGATE` | `@Binds` method in a module |
| `COMPONENT_PROVISION` | Exposed from component interface |
| `COMPONENT_DEPENDENCY` | From a component dependency |
| `MULTIBOUND_SET` | Set multibinding (`@IntoSet`) |
| `MULTIBOUND_MAP` | Map multibinding (`@IntoMap`) |
| `OPTIONAL` | Optional binding |
| `MEMBERS_INJECTOR` | Members injector |
| `ASSISTED_INJECTION` | Assisted injection |
| `ASSISTED_FACTORY` | Assisted factory |
| `BOUND_INSTANCE` | `@BindsInstance` parameter |
| `SUBCOMPONENT_CREATOR` | Subcomponent factory/builder |
| `PRODUCTION` | Dagger Producers binding |

## Example GEXF Output

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gexf xmlns="http://gexf.net/1.3" version="1.3">
  <meta>
    <creator>AppGraphDaggerPlugin</creator>
    <description>Dagger binding graph for AppComponent</description>
  </meta>
  <graph mode="static" defaultedgetype="directed">
    <attributes class="node">
      <attribute id="nodeType" title="Node Type" type="string"/>
      <attribute id="bindingKind" title="Binding Kind" type="string"/>
      <attribute id="scope" title="Scope" type="string"/>
    </attributes>
    <nodes>
      <node id="n1" label="UserRepository">
        <attvalues>
          <attvalue for="nodeType" value="BINDING"/>
          <attvalue for="bindingKind" value="INJECTION"/>
        </attvalues>
      </node>
      <node id="n2" label="ApiService">
        <attvalues>
          <attvalue for="nodeType" value="BINDING"/>
          <attvalue for="bindingKind" value="PROVISION"/>
          <attvalue for="scope" value="@Singleton"/>
        </attvalues>
      </node>
    </nodes>
    <edges>
      <edge id="e1" source="n1" target="n2">
        <attvalues>
          <attvalue for="edgeType" value="DEPENDENCY"/>
        </attvalues>
      </edge>
    </edges>
  </graph>
</gexf>
```

## Visualization Tools

### Gephi

[Gephi](https://gephi.org/) is the recommended tool for viewing GEXF files:

1. **Import**: File > Open > Select your .gexf file
2. **Layout**: Use "Force Atlas 2" or "Fruchterman Reingold"
3. **Appearance**: Color nodes by `nodeType` attribute
4. **Filters**: Filter by node type or binding kind

### Other Tools

GEXF is supported by many graph tools:

- [NetworkX](https://networkx.org/) (Python)
- [Cytoscape](https://cytoscape.org/)
- [yEd](https://www.yworks.com/products/yed)
- [Graph-tool](https://graph-tool.skewed.de/)

## Programmatic Access

You can also parse GEXF files programmatically for custom analysis:

```kotlin
import javax.xml.parsers.DocumentBuilderFactory

val factory = DocumentBuilderFactory.newInstance()
val builder = factory.newDocumentBuilder()
val document = builder.parse(File("AppComponent.gexf"))

// Query nodes, edges, attributes...
```
