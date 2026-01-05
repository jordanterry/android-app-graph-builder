package sample

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

/**
 * Module providing network-related dependencies.
 * Demonstrates @ContributesTo and @Provides patterns in Metro.
 */
@ContributesTo(AppGraph::class)
interface NetworkModule {

    @Provides
    fun provideApiClient(): ApiClient = ApiClient("https://api.example.com")

    @Provides
    fun provideLogger(): Logger = ConsoleLogger()
}

/**
 * Simple API client.
 */
data class ApiClient(val baseUrl: String)

/**
 * Logger interface.
 */
interface Logger {
    fun log(message: String)
}

/**
 * Console implementation of Logger.
 */
class ConsoleLogger : Logger {
    override fun log(message: String) {
        println("[LOG] $message")
    }
}
