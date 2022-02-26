import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.*

/** Returns the path to the executing binary. */
val fullBinaryPath: String by lazy {
    memScoped {
        val length = PATH_MAX.toULong()
        val pathBuf = allocArray<ByteVar>(length.toInt())
        val myPid = getpid()
        val res = readlink("/proc/$myPid/exe", pathBuf, length)
        if (res < 1)
            throw RuntimeException("/proc/$myPid/exe failed: $res")

        pathBuf.toKString()
    }
}

fun String.chop(): String {
    val pathSepIndex = lastIndexOf('/')
    if (pathSepIndex == -1)
        throw RuntimeException("Could not locate containing directory of $this")
    return subSequence(0, pathSepIndex).toString()
}


val STDERR = platform.posix.fdopen(2, "w")
fun errPrint(message: Any? = "") {
    fprintf(STDERR, message.toString())
    fflush(STDERR)
}
fun errPrintln(message: Any? = "") {
    fprintf(STDERR, message.toString() + "\n")
    fflush(STDERR)
}
