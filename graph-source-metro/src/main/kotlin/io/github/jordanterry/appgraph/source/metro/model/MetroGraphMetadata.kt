package io.github.jordanterry.appgraph.source.metro.model

import kotlinx.serialization.Serializable

/**
 * Root structure of Metro's graph metadata JSON.
 *
 * Output location: {reportsDestination}/{sourceSet}/graph-metadata/graph-{GraphName}.json
 */
@Serializable
data class MetroGraphMetadata(
    val graph: String,
    val scopes: List<String> = emptyList(),
    val aggregationScopes: List<String> = emptyList(),
    val roots: MetroRoots = MetroRoots(),
    val extensions: MetroExtensions = MetroExtensions(),
    val bindings: List<MetroBinding> = emptyList()
)

@Serializable
data class MetroRoots(
    val accessors: List<MetroAccessor> = emptyList(),
    val injectors: List<MetroInjector> = emptyList()
)

@Serializable
data class MetroAccessor(
    val key: String,
    val isDeferrable: Boolean = false
)

@Serializable
data class MetroInjector(
    val key: String
)

@Serializable
data class MetroExtensions(
    val accessors: List<MetroAccessor> = emptyList(),
    val factoryAccessors: List<MetroAccessor> = emptyList(),
    val factoriesImplemented: List<String> = emptyList()
)

@Serializable
data class MetroBinding(
    val key: String,
    val bindingKind: String,
    val isScoped: Boolean = false,
    val nameHint: String? = null,
    val dependencies: List<MetroDependency> = emptyList(),
    val isSynthetic: Boolean = false,
    val origin: String? = null,
    val declaration: String? = null,
    val multibinding: MetroMultibinding? = null,
    val optionalWrapper: String? = null,
    val aliasTarget: String? = null
)

@Serializable
data class MetroDependency(
    val key: String,
    val hasDefault: Boolean = false,
    val isAssisted: Boolean = false
)

@Serializable
data class MetroMultibinding(
    val contributionType: String? = null,
    val mapKey: String? = null
)
