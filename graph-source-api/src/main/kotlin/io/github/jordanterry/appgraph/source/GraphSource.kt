package io.github.jordanterry.appgraph.source

import io.github.jordanterry.appgraph.model.Graph
import java.nio.file.Path

/**
 * Abstraction for extracting dependency graphs from various DI frameworks.
 *
 * Implementations can be:
 * - Compile-time (e.g., Dagger SPI plugin)
 * - Post-compile file readers (e.g., Metro JSON metadata)
 */
interface GraphSource {
    /** Unique identifier for this source type (e.g., "dagger", "metro") */
    val sourceType: String

    /** Human-readable name for display */
    val displayName: String

    /**
     * Extract graphs from the given input.
     *
     * @param input Configuration for the source (paths, options, etc.)
     * @return Result containing extracted graphs or errors
     */
    fun extract(input: GraphSourceInput): GraphSourceResult
}

/**
 * Input configuration for graph extraction.
 */
sealed interface GraphSourceInput {
    /** Input paths (directories or files) to read from */
    val paths: List<Path>
}

/**
 * Metro-specific input configuration.
 */
data class MetroGraphSourceInput(
    override val paths: List<Path>,
    val sourceSet: String = "main"
) : GraphSourceInput

/**
 * Result of graph extraction.
 */
sealed interface GraphSourceResult {
    /**
     * Successful extraction of all graphs.
     */
    data class Success(val graphs: List<Graph>) : GraphSourceResult

    /**
     * Complete failure to extract any graphs.
     */
    data class Error(val message: String, val cause: Throwable? = null) : GraphSourceResult

    /**
     * Partial success - some graphs extracted, but some errors occurred.
     */
    data class Partial(val graphs: List<Graph>, val errors: List<String>) : GraphSourceResult
}
