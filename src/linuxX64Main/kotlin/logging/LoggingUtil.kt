package logging

import mu.KotlinLogging
import mu.internal.ErrorMessageProducer
import kotlin.reflect.*

@Suppress("NOTHING_TO_INLINE")
internal inline fun (() -> Any?).toStringSafe(): String {
    return try {
        invoke().toString()
    } catch (e: Exception) {
        ErrorMessageProducer.getErrorLog(e)
    }
}


inline fun Any.getLogger(loggerName: String? = null): KLoggerCustom {
    val name = loggerName ?: this::class.qualifiedName ?: ""
    return KLoggerCustom(KotlinLogging.logger(name), name)
}


inline fun getLogger(loggerName: String): KLoggerCustom {
    return KLoggerCustom(KotlinLogging.logger(loggerName), loggerName)
}

private  fun Any.getLogger(loggerName: String? = null, recursion: Int = 0): KLoggerCustom {
    val name = loggerName ?: this::class.qualifiedName ?: ""
    val displayName = if(recursion > 0) {
        name + List(recursion) { '^' }.joinToString("")
    } else {
        name
    }
    return KLoggerCustom(KotlinLogging.logger(displayName), name, recursion)
}
private fun KLoggerCustom.getLoggerForCall(functionName: String): KLoggerCustom {
    return if(name.endsWith(".$functionName")) {
        getLogger(this.name, recursion = recursion + 1)
    } else if(recursion > 0){
        getLogger(name + List(recursion) { '^' }.joinToString("") + "." + functionName, recursion = 0)
    } else {
        getLogger(this.name + "." + functionName, recursion = 0)
    }
}

private fun <R> R.logExit(logger: KLoggerCustom): R {
    return logger.exit(this)
}

fun <R> KLoggerCustom.loggedCall(function: KFunction1<KLoggerCustom, R>): R {
    val logger = getLoggerForCall(function.name)
    logger.entry(function.name)
    return function(logger)
        .logExit(logger)
}
fun <A, R> KLoggerCustom.loggedCall(function: KFunction2<KLoggerCustom, A, R>, argA: A): R {
    val logger = getLoggerForCall(function.name)
    logger.entry(function.name, argA)
    return function(logger, argA)
        .logExit(logger)
}
fun <A, B, R> KLoggerCustom.loggedCall(function: KFunction3<KLoggerCustom, A, B, R>, argA: A, argB: B): R {
    val logger = getLoggerForCall(function.name)
    logger.entry(function.name, argA, argB)
    return function(logger, argA, argB)
        .logExit(logger)
}