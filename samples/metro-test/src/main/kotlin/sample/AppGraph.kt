package sample

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Inject

/**
 * Main application dependency graph.
 * This demonstrates a simple Metro DI setup for testing graph metadata generation.
 */
@DependencyGraph
interface AppGraph {
    /** Entry point to get the UserService */
    val userService: UserService

    /** Entry point to get the AnalyticsService */
    val analyticsService: AnalyticsService
}

/**
 * Repository for user data.
 */
class UserRepository @Inject constructor()

/**
 * Service for user operations.
 * Depends on UserRepository.
 */
class UserService @Inject constructor(
    private val repository: UserRepository
)

/**
 * Service for analytics.
 */
class AnalyticsService @Inject constructor()
