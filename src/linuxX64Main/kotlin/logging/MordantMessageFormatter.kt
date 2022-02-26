package logging

import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.TerminalColors
import mu.Formatter
import mu.KotlinLoggingLevel
import mu.Marker

class MordantMessageFormatter(
    private val c: TerminalColors
) : Formatter {
    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        msg: () -> Any?
    ): String =
        "${level.format(":")} ${formatName(loggerName)} ${c.plain(msg.toStringSafe())}"

    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        t: Throwable?,
        msg: () -> Any?
    ): String =
        "${level.format(":")} ${formatName(loggerName)} ${c.plain(msg.toStringSafe())} ${t.throwableToString()}"

    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        marker: Marker?,
        msg: () -> Any?
    ): String =
        "${level.format(":")} ${formatName(loggerName)} ${marker?.getName()?.textStyle(c.brightYellow)} ${c.plain(msg.toStringSafe())}"

    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        marker: Marker?,
        t: Throwable?,
        msg: () -> Any?
    ): String =
        "${level.format(":")} ${formatName(loggerName)} ${marker?.getName()?.textStyle(c.brightYellow)} ${c.plain(msg.toStringSafe())} ${t.throwableToString()}"

    private fun Throwable?.throwableToString(): String {
        if (this == null) {
            return ""
        }
        var msg = ""
        var current = this
        while (current != null && current.cause != current) {
            val stacktrace = current.getStackTrace().joinToString("\n    ", prefix = "\n    ")
            msg += "\n  "
            msg += c.danger("Caused by: '${current.message}'")
            msg += c.red(stacktrace)
            current = current.cause
        }
        return msg
    }

    private val maxLevelNameLength: Int by lazy {
        KotlinLoggingLevel.values().maxOf { it.name.length }
    }

    private fun KotlinLoggingLevel.format(postfix: String = ":"): String {
        val color = when(this) {
            KotlinLoggingLevel.TRACE -> c.muted
            KotlinLoggingLevel.DEBUG -> c.brightCyan
            KotlinLoggingLevel.INFO -> c.info
            KotlinLoggingLevel.WARN -> c.warning
            KotlinLoggingLevel.ERROR -> c.danger
        }

        val padLength = maxLevelNameLength - name.length
        val text = (color(name) + postfix)
        return text.padEnd(text.length + padLength, ' ')
    }

    private fun formatName(loggerName: String): String {
        if(loggerName.isBlank()) return c.gray("_")
        val darkGray = c.gray + c.dim
        return "${darkGray("[")}${c.gray(loggerName)}${darkGray("]")}"
    }
}

private fun String.textStyle(textStyle: TextStyle): String = textStyle(this)

