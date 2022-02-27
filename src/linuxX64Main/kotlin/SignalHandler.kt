import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import logging.getLogger
import platform.posix.exit
import platform.posix.strsignal

typealias OnSignalCallback = suspend (signalNumber: Int, signalName: String) -> Unit

object SignalHandler {
    private val signalCallbacks: MutableMap<Int, MutableList<OnSignalCallback>> =
        mutableMapOf()

    private val logger = getLogger()

    fun handleSignal(signalNumber: Int) = runBlocking {
        errPrintln()
        errPrintln()
        val signalName = strsignal(signalNumber)?.toKString() ?: "Unknown"
        logger.warn { "received signal: $signalNumber $signalName" }

        signalCallbacks[signalNumber]?.forEach { callback ->
            callback.invoke(signalNumber, signalName)
        }
    }

    fun onSignal(
        vararg signals: Int,
        onSignalCallback: OnSignalCallback
    ) {
        signals.forEach { signal ->
            signalCallbacks.getOrPut(signal) { mutableListOf() } += onSignalCallback
        }
    }
}
