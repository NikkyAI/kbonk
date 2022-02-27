package server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import nikky.moe.plugins.configureHTTP
import server.plugins.configureRouting
import kotlin.coroutines.*

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port]
 * @param watchPaths specifies path substrings that will be watched for automatic reloading
 * @param configure configuration script for the engine
 * @param module application module function
 */
@OptIn(DelicateCoroutinesApi::class)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    logger: Logger = KtorSimpleLogger("ktor.application"),
    watchPaths: List<String> = listOf(),
    configure: TConfiguration.() -> Unit = {},
    module: Application.() -> Unit
): TEngine = GlobalScope.embeddedServer(factory, port, host, logger, watchPaths, EmptyCoroutineContext, configure, module)

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port]
 * @param watchPaths specifies path substrings that will be watched for automatic reloading
 * @param configure configuration script for the engine
 * @param parentCoroutineContext specifies a coroutine context to be used for server jobs
 * @param module application module function
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
        CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    logger: Logger = KtorSimpleLogger("ktor.application"),
    watchPaths: List<String> = listOf(),
    parentCoroutineContext: CoroutineContext = coroutineContext,
    configure: TConfiguration.() -> Unit = {},
    module: Application.() -> Unit
): TEngine {
    val connectors: Array<EngineConnectorConfig> = arrayOf(
        EngineConnectorBuilder().apply {
            this.port = port
            this.host = host
        }
    )
    return embeddedServer(
        factory = factory,
        connectors = connectors,
        logger = logger,
        watchPaths = watchPaths,
        parentCoroutineContext = parentCoroutineContext,
        configure = configure,
        module = module
    )
}

/**
 * Creates an embedded server with the given [factory], listening on given [connectors]
 * @param connectors default listening on 0.0.0.0:80
 * @param watchPaths specifies path substrings that will be watched for automatic reloading
 * @param parentCoroutineContext specifies a coroutine context to be used for server jobs
 * @param configure configuration script for the engine
 * @param module application module function
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    vararg connectors: EngineConnectorConfig = arrayOf(EngineConnectorBuilder()),
    logger: Logger = KtorSimpleLogger("ktor.application"),
    watchPaths: List<String> = listOf(),
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    configure: TConfiguration.() -> Unit = {},
    module: Application.() -> Unit
): TEngine {
    val environment = applicationEngineEnvironment {
        this.parentCoroutineContext = coroutineContext + parentCoroutineContext
        this.log = logger
        this.watchPaths = watchPaths
        this.module(module)
        this.connectors.addAll(connectors)
    }

    return embeddedServer(factory, environment, configure)
}
