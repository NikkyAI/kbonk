package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.server.application.*
import io.ktor.server.cio.*
import kotlinx.coroutines.runBlocking
import nikky.moe.plugins.configureHTTP
import platform.posix.sleep
import server.plugins.configureRouting
import platform.posix.*
import kotlinx.cinterop.*
import logging.getLogger
import server.embeddedServer
import server
import server.KtorLogger
import serverRunning

class ServerOptions : OptionGroup() {
    private val DEFAULT_PORT by lazy {
        getenv("PORT")?.toKString()?.toIntOrNull() ?: 1337
    }
    private val DEFAULT_BIND by lazy {
        getenv("BIND")?.toKString() ?: "0.0.0.0"
    }
    val port: Int by option("--port")
        .int()
        .default(DEFAULT_PORT)
        .check("must be valid port numnber") { port ->
            port in 1024..65535
        }
    val bind: String by option("--bind")
        .default(DEFAULT_BIND)
}

class ServeCommand() : CliktCommand(
    name = "serve"
) {
    private val logger = getLogger()
    val serverOptions: ServerOptions by ServerOptions()
    override fun run() {
        runBlocking {
            runServer(serverOptions)
        }
    }

    suspend fun runServer(serverOptions: ServerOptions) {
        logger.info { "starting server on ${serverOptions.bind}:${serverOptions.port}" }

        try {
            server = embeddedServer(
                factory = CIO,
                port = serverOptions.port,
                host = serverOptions.bind,
                logger = KtorLogger()
            ) {
                this.log
                configureHTTP()
                configureRouting()
            }.start(wait = false)

//        server.addShutdownHook {
//            println("stopping server")
//        }

            serverRunning = true

            while (serverRunning) {
                sleep(200)
            }
        } catch (e: io.ktor.utils.io.errors.PosixException.AddressAlreadyInUseException) {
            error("cannot bind to ${serverOptions.bind}:${serverOptions.port}")
        }

    }
}

