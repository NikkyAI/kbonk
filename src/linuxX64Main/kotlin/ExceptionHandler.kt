import io.ktor.utils.io.errors.*
import kotlinx.coroutines.CoroutineExceptionHandler
import logging.getLogger
import platform.posix.exit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

object ExceptionHandler {

    private val logger = getLogger()

    fun handleException(e: Throwable) {
        println("")
        when(e) {
            is CancellationException -> {
                logger.error(e.cause ?: e) { "cancelled by" }
                exit(-1)
            }
//        is PosixException.AddressAlreadyInUseException -> {
//            logger.error { "caught error ${e.errno} ${e.message}" }
//            logger.error { "cannot bind to ${e..host}:${serverOptions.port}" }
//            logger.error { "cannot bind to ${serverOptions.host}:${serverOptions.port}" }
//            exit(-1)
//        }
            is PosixException -> {
                logger.error { "caught error ${e.message}" }
                exit(-1)
            }
            else -> {
                logger.error(e) { "unhandled exception" }
                exit(-1)
            }
        }
    }
    val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { coroutineContext, e ->
        handleException(e)
    } as CoroutineContext
}