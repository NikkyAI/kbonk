import com.github.ajalt.mordant.terminal.*
import io.ktor.server.engine.*
import kotlinx.cinterop.staticCFunction
import platform.posix.*
import commands.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import logging.MordantAppender
import logging.MordantMessageFormatter
import logging.StderrTerminalInterface
import logging.getLogger
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import kotlin.time.ExperimentalTime

lateinit var server: ApplicationEngine
var serverRunning: Boolean = false

@OptIn(ExperimentalTime::class)
fun handleSignal(signalNumber: Int) = runBlocking {
    errPrintln()
    errPrintln("Interrupt: $signalNumber")

    //TODO: make my own shutdown hooks
    withTimeout(5_000) {
        if (::server.isInitialized) {
            errPrintln("stopping server")
            serverRunning = false
            server.stop(500, 500)
        }
    }
    exit(0)
}

private val logger = getLogger("main")

@OptIn(ExperimentalTerminalApi::class)
fun main(vararg args: String) {
    val stderrTerminal = Terminal(
        terminalInterface = StderrTerminalInterface(Terminal().info)
    )
    KotlinLoggingConfiguration.logLevel = KotlinLoggingLevel.DEBUG
    KotlinLoggingConfiguration.appender = MordantAppender(stderrTerminal)
    KotlinLoggingConfiguration.formatter = MordantMessageFormatter(stderrTerminal.colors)

    logger.debug { "installing signal handlers" }
    signal(SIGINT, staticCFunction(::handleSignal))
//    signal(SIGTERM, staticCFunction(::handleSignal))

    KBonkCommand().main(args)
}

