---
sidebar_position: 3
---

# Configuration

AppGraph provides an `appGraph` extension block for configuring the plugin behavior.

## Extension Options

```kotlin
appGraph {
    enabled.set(true)                              // Enable/disable graph generation
    outputPath.set("build/reports/dagger-graph")   // Custom output directory
    outputFormat.set("gexf")                       // Output format
}
```

### enabled

| Property | Type | Default |
|----------|------|---------|
| `enabled` | `Boolean` | `true` |

Controls whether graph generation is enabled. Set to `false` to temporarily disable the plugin without removing it.

```kotlin
appGraph {
    enabled.set(false)  // Disable graph generation
}
```

This is useful for:
- Speeding up builds during development
- Disabling in CI for certain build types
- Conditional enabling based on build variants

### outputPath

| Property | Type | Default |
|----------|------|---------|
| `outputPath` | `String` | `"build/generated/appgraph"` |

Specifies the directory where GEXF files will be written.

```kotlin
appGraph {
    outputPath.set("build/reports/dagger-graph")
}
```

The output directory is created automatically if it doesn't exist.

### outputFormat

| Property | Type | Default |
|----------|------|---------|
| `outputFormat` | `String` | `"gexf"` |

Specifies the output format. Currently only GEXF is supported.

```kotlin
appGraph {
    outputFormat.set("gexf")
}
```

## Full Example

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("io.github.jordanterry.appgraph") version "0.0.1"
}

android {
    // ... your Android configuration
}

appGraph {
    enabled.set(true)
    outputPath.set("build/reports/dagger-graph")
    outputFormat.set("gexf")
}

dependencies {
    implementation("com.google.dagger:dagger:2.51.1")
    kapt("com.google.dagger:dagger-compiler:2.51.1")
}
```

## Build Variant Configuration

You can conditionally configure Grph based on build variants:

```kotlin
android {
    // ...
}

// Only enable for debug builds
afterEvaluate {
    val isDebug = gradle.startParameter.taskNames.any {
        it.contains("debug", ignoreCase = true)
    }
    appGraph {
        enabled.set(isDebug)
    }
}
```

## Annotation Processor Arguments

Under the hood, the plugin passes these annotation processor arguments to Dagger:

| Argument | Description |
|----------|-------------|
| `appgraph.enabled` | Whether graph generation is enabled |
| `appgraph.output.path` | Output directory for GEXF files |
| `appgraph.output.format` | Output format (gexf) |

These are set automatically based on your `appGraph {}` configuration.
