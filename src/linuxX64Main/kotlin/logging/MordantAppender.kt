package logging

import com.github.ajalt.mordant.terminal.Terminal
import mu.Appender

public class MordantAppender(
    private val terminal: Terminal
) : Appender {
    override fun trace(message: Any?) {
        terminal.println(message)
    }
    override fun debug(message: Any?) {
        terminal.println(message)
    }
    override fun info(message: Any?) {
        terminal.println(message)
    }
    override fun warn(message: Any?) {
        terminal.println(message)
    }
    override fun error(message: Any?) {
        terminal.println(message)
    }
}