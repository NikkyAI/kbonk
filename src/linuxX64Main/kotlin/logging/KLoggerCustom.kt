package logging

import mu.KLogger
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel

class KLoggerCustom(val logger: KLogger, val name: String, val recursion: Int = 0): KLogger by logger {
    override fun entry(vararg argArray: Any?) {
        trace { "-> ${argArray.firstOrNull() ?: ""}(${argArray.drop(1).joinToString(", ")})" }
    }

    override fun <T : Any?> exit(result: T): T {
        trace {
            if(result is Unit) {
                "<-"
            } else {
                "<- $result"
            }
        }
        return result
    }

    override fun <T : Throwable> throwing(throwable: T): T {
        error(throwable) { "throwing($throwable)" }
        return throwable
    }

    override fun <T : Throwable> catching(throwable: T) {
        error(throwable) { "catching($throwable)" }
    }
}