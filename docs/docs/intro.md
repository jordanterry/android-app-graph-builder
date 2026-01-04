---
sidebar_position: 1
---

# Introduction

**AppGraph** is a Gradle plugin that generates visual graph representations of your [Dagger](https://dagger.dev/) dependency injection graphs. It outputs graphs in [GEXF format](https://gexf.net/), which can be visualized in tools like [Gephi](https://gephi.org/).

## Why Visualize Dagger Graphs?

As Android applications grow, their dependency injection graphs become increasingly complex. AppGraph helps you:

- **Understand your architecture** - See how components, modules, and bindings connect
- **Identify issues** - Spot missing bindings, circular dependencies, or unexpected relationships
- **Document your system** - Generate visual documentation of your DI structure
- **Onboard new team members** - Help developers understand the codebase faster

## What Gets Captured

AppGraph extracts the complete resolved binding graph from Dagger, including:

- **Components** - `@Component` and `@Subcomponent` classes with their scopes
- **Bindings** - `@Inject` constructors, `@Provides` methods, `@Binds` methods
- **Multibindings** - `@IntoSet`, `@IntoMap` contributions
- **Dependencies** - Relationships between all bindings
- **Scopes** - `@Singleton`, custom scopes, and unscoped bindings

## Quick Example

After applying the plugin and building your project, you'll get a GEXF file for each Dagger component:

```
build/generated/appgraph/
└── AppComponent.gexf
```

Open this file in Gephi to visualize your dependency graph.

## Next Steps

- [Getting Started](./getting-started) - Install and configure the plugin
- [Configuration](./configuration) - Customize output path and format
- [Output Format](./output-format) - Understand the GEXF output structure
