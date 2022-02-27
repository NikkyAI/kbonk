package server

import io.ktor.util.logging.*
import logging.getLogger
import mu.KLogger

class KtorKLogger(private val logger: KLogger = getLogger("ktor.application")) : Logger {
    override fun error(message: String) {
        logger.error { message }
    }

    override fun error(message: String, cause: Throwable) {
        logger.error(cause) { message }
    }

    override fun warn(message: String) {
        logger.warn { message }
    }

    override fun warn(message: String, cause: Throwable) {
        logger.warn(cause) { message }
    }

    override fun info(message: String) {
        logger.info { message }
    }

    override fun info(message: String, cause: Throwable) {
        logger.info(cause) { message }
    }

    override fun debug(message: String) {
        logger.debug { message }
    }

    override fun debug(message: String, cause: Throwable) {
        logger.debug(cause) { message }
    }

    override fun trace(message: String) {
        logger.trace { message }
    }

    override fun trace(message: String, cause: Throwable) {
        logger.trace(cause) { message }
    }


}
