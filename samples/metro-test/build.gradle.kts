plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.metro)
}

metro {
    // Enable graph metadata generation
    reportsDestination.set(layout.buildDirectory.dir("reports/metro"))
}

// Metro plugin automatically adds the runtime dependency

dependencies {
    // For the conversion task
    testImplementation(project(":graph-source-metro"))
    testImplementation(project(":graph-writer-gexf"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

// Task to verify Metro graph metadata was generated
tasks.register("verifyMetroGraphMetadata") {
    dependsOn("compileKotlin")
    doLast {
        val metadataDir = layout.buildDirectory.dir("reports/metro/jvmMain/graph-metadata").get().asFile
        if (metadataDir.exists()) {
            val files = metadataDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
            if (files.isNotEmpty()) {
                println("Metro graph metadata generated successfully:")
                files.forEach { file ->
                    println("  - ${file.name}")
                    println("    Content preview:")
                    println(file.readText().take(500))
                    println("...")
                }
            } else {
                println("No Metro graph metadata JSON files found in: $metadataDir")
            }
        } else {
            println("Metro metadata directory not found: $metadataDir")
            println("Checking alternative locations...")
            val reportsDir = layout.buildDirectory.dir("reports/metro").get().asFile
            if (reportsDir.exists()) {
                println("Reports dir exists, contents:")
                reportsDir.walkTopDown().forEach { println("  ${it.relativeTo(reportsDir)}") }
            }
        }
    }
}
