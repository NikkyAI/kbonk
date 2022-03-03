package commands

import SignalHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.server.cio.*
import nikky.moe.plugins.configureHTTP
import server.plugins.configureRouting
import platform.posix.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import logging.getLogger
import server.KtorKLogger
import server.embeddedServer


class ServeCommand() : CliktCommand(
    name = "serve"
) {
    private val logger = getLogger()
    val serverOptions: ServerOptions by ServerOptions()
    override fun run() {
        serve(serverOptions)
    }

    private fun serve(serverOptions: ServerOptions) = runBlocking {
        logger.info { "serving on ${serverOptions.host}:${serverOptions.port}" }

//        val completableJob = SupervisorJob()
//        SignalHandler.onSignal(SIGINT) { _, signalName ->
//            logger.warn { "received signal $signalName, stopping server.." }
//            completableJob.cancel()
//        }
//        embeddedServer(
//            factory = CIO,
//            host = serverOptions.host,
//            port = serverOptions.port,
//            logger = KtorKLogger(),
//            parentCoroutineContext = completableJob
//        ) {
//            configureHTTP()
//            configureRouting()
//        }.start(true)
//        println("stopped")
//        logger.info { "server stopped" }
//        exit(0)

        val server = embeddedServer(
            factory = CIO,
            host = serverOptions.host,
            port = serverOptions.port,
            logger = KtorKLogger(),
        ) {
            configureHTTP()
            configureRouting()
        }

        logger.info { "server starting" }
        server.start(wait = false)

        val completableJob = SupervisorJob()
        SignalHandler.onSignal(SIGINT) { _, signalName ->
            logger.warn { "received signal $signalName, continuing to stop server" }
            completableJob.complete()
        }
        logger.debug { "blocking until interrupted" }
        completableJob.join()

//        val stopSignal = Semaphore(1, 1)
//        SignalHandler.onSignal(SIGINT) { _, signalName ->
//            logger.warn { "received signal $signalName, continuing.." }
//            stopSignal.release()
//        }
//        logger.debug { "blocking until interrupted" }
//        stopSignal.acquire()

//        suspendCoroutine<Unit> { cont ->
//            logger.trace { "registering shutdown callback" }
//            SignalHandler.onSignal(SIGINT) { _, _ ->
//                logger.warn { "shutdown callback called, continuing.." }
//                cont.resume(Unit)
//            }
//            logger.debug { "registered shutdown callback" }
//            logger.debug { "blocking until interrupted" }
//        }

        logger.info { "stopping server" }
        server.stop()
        logger.info { "server stopped" }
        exit(0)
    }

    class ServerOptions : OptionGroup() {
        private val DEFAULT_PORT by lazy {
            getenv("PORT")?.toKString()?.toIntOrNull() ?: 1337
        }
        private val DEFAULT_HOST by lazy {
            getenv("BIND")?.toKString() ?: "0.0.0.0"
        }
        val port: Int by option("--port")
            .int()
            .default(DEFAULT_PORT)
            .check("must be valid port numnber") { port ->
                port in 1024..65535
            }
        val host: String by option("--host")
            .default(DEFAULT_HOST)
    }
}

