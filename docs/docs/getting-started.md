---
sidebar_position: 2
---

# Getting Started

This guide will help you set up AppGraph in your Android or JVM project.

## Prerequisites

Before you begin, ensure you have:

- **Gradle** 8.0 or higher
- **Dagger** 2.x configured in your project
- **Kotlin** with kapt (or Java annotation processing)

## Installation

### Step 1: Add the Plugin Repository

Since AppGraph is currently published to Maven Local, you need to add `mavenLocal()` to your repositories.

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

### Step 2: Apply the Plugin

In your module's `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") // or com.android.library
    kotlin("android")
    kotlin("kapt")
    id("io.github.jordanterry.appgraph") version "0.0.1"
}
```

### Step 3: Ensure Dagger is Configured

Make sure you have Dagger set up:

```kotlin
dependencies {
    implementation("com.google.dagger:dagger:2.51.1")
    kapt("com.google.dagger:dagger-compiler:2.51.1")
}
```

## Basic Usage

Once the plugin is applied, it automatically runs during compilation. Simply build your project:

```bash
./gradlew build
```

After a successful build, you'll find GEXF files in:

```
build/generated/appgraph/
├── AppComponent.gexf
├── FeatureComponent.gexf
└── ...
```

Each Dagger `@Component` gets its own GEXF file.

## Viewing the Graph

The generated GEXF files can be opened in graph visualization tools:

### Using Gephi (Recommended)

1. Download and install [Gephi](https://gephi.org/)
2. Open Gephi and go to **File > Open**
3. Select your `.gexf` file
4. Use the **Layout** panel to arrange nodes (try "Force Atlas 2")
5. Explore your dependency graph!

### Tips for Gephi

- Use **Filters** to focus on specific node types (components, bindings, etc.)
- Color nodes by their `nodeType` attribute to distinguish components from bindings
- Use **Statistics > Modularity** to identify clusters in your graph

## Next Steps

- [Configuration](./configuration) - Customize output paths and enable/disable the plugin
- [Output Format](./output-format) - Understand the structure of GEXF output
