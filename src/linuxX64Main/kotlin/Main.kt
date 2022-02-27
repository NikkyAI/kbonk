import com.github.ajalt.mordant.terminal.*
import kotlinx.cinterop.staticCFunction
import platform.posix.*
import commands.*
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import logging.MordantAppender
import logging.MordantMessageFormatter
import logging.StderrTerminalInterface
import logging.getLogger
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTerminalApi::class)
fun main(vararg args: String) {
    val logger = getLogger("main")
    val stderrTerminal = Terminal(
        terminalInterface = StderrTerminalInterface(Terminal().info)
    )
    KotlinLoggingConfiguration.logLevel = KotlinLoggingLevel.DEBUG
    KotlinLoggingConfiguration.appender = MordantAppender(stderrTerminal)
    KotlinLoggingConfiguration.formatter = MordantMessageFormatter(stderrTerminal.colors)

    logger.debug { "installing exception handler" }
    setUnhandledExceptionHook(ExceptionHandler::handleException)

    logger.debug { "installing signal handlers" }
    signal(SIGINT, staticCFunction(::handleSignal))

    MainCommand().main(args)
}

private fun handleSignal(signalNumber: Int) {
    SignalHandler.handleSignal(signalNumber)
}