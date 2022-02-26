package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import logging.getLogger
import mu.KMarkerFactory
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import errPrintln
import logging.KLoggerCustom
import logging.loggedCall
import magickwand.time
import mu.KLogger
import kotlin.reflect.KFunction1

class TestCommand() : CliktCommand(
    name = "test"
) {
    private val logger = getLogger()
    override fun run() {
        val oldLoglevel = KotlinLoggingConfiguration.logLevel
        KotlinLoggingConfiguration.logLevel = KotlinLoggingLevel.TRACE

        logger.entry("run", 1, 2, 3)

        val c = Terminal(AnsiLevel.TRUECOLOR).colors
        listOf(
            "black" to c.black,
            "red" to c.red,
            "green" to c.green,
            "yellow" to c.yellow,
            "blue" to c.blue,
            "magenta" to c.magenta,
            "cyan" to c.cyan,
            "white" to c.white,
            "gray" to c.gray,
            "brightRed" to c.brightRed,
            "brightGreen" to c.brightGreen,
            "brightYellow" to c.brightYellow,
            "brightBlue" to c.brightBlue,
            "brightMagenta" to c.brightMagenta,
            "brightCyan" to c.brightCyan,
            "brightWhite" to c.brightWhite,
            "success" to c.success,
            "danger" to c.danger,
            "warning" to c.warning,
            "info" to c.info,
            "muted" to c.muted,
        ).forEach { (name, color) ->
            logger.info { "${name}: ${color(name)}" }
        }

        errPrintln()

        val CUSTOM_MARKER = KMarkerFactory.getMarker("CUSTOM")
        logger.trace { "trace" }
        logger.debug { "debug" }
        logger.info { "info" }
        logger.warn { "warn" }
        logger.error { "error" }
//        logger.error(Exception("an exception")) { "exception" }
//        logger.entry()
        logger.info(CUSTOM_MARKER) { "we can has custom marker, not sure what for... yet?" }
        // useless
        logger.catching(Exception("another Exception"))
        logger.throwing(Exception("another Exception"))
//        logger.exit("success")

        val result = logger.loggedCall(function = ::times, 6, 7)
        logger.loggedCall(function = ::doStuff, result)

        logger.loggedCall(::fibonacci, 5)

        logger.loggedCall(::countDownTo, 3, ::onCountDownEnd)

        KotlinLoggingConfiguration.logLevel = oldLoglevel
    }

    private fun doStuff(logger: KLogger, number: Int) {
        logger.info { "received: $number" }
    }

    private fun times(logger: KLogger, a: Int, b: Int): Int {
        logger.info { "multiplying numbers: $a * $b" }
        return a * b
    }


    private fun fibonacci(
        logger: KLoggerCustom, n: Long
    ): Long = if (n < 2)
        n
    else
        logger.loggedCall(::fibonacci, n - 1) +  logger.loggedCall(::fibonacci, n - 2)

    private fun onCountDownEnd(logger: KLoggerCustom): Int {
        logger.warn { "boom" }
        logger.loggedCall(::countUpTo, 0, ::onCountUpEnd)
        return 0
    }
    private fun countDownTo(logger: KLoggerCustom, count: Int = 5, then: KFunction1<KLoggerCustom, Int>) {
        if(count <= 0) {
            logger.loggedCall(then)
            return
        }
        logger.loggedCall(::countDownTo, count - 1, then)
    }
    private fun onCountUpEnd(logger: KLoggerCustom): Int {
        logger.warn { "boom" }
        return 0
    }
    private fun countUpTo(logger: KLoggerCustom, count: Int, then: KFunction1<KLoggerCustom, Int>) {
        if(count >= 3) {
            logger.loggedCall(then)
            return
        }
        logger.loggedCall(::countUpTo, count + 1, then)
    }
}

