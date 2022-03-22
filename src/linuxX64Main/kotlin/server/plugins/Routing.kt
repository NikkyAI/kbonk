package server.plugins

import BonkMagick
import WandException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import logging.getLogger
import magickwand.NorthGravity
import magickwand.SouthGravity
import platform.posix.SIGPIPE
import resources.*

fun Application.configureRouting() {
    val logger = getLogger("Server")

    runBlocking {
        SignalHandler.onSignal(
            SIGPIPE
        ) { signalNumber: Int, signalName: String ->
            logger.error { "received signal $signalNumber $signalName" }
        }
    }

    install(ContentNegotiation) {
        json()
    }

    routing {
        suspend fun PipelineContext<Unit, ApplicationCall>.respondBonk(
            text: String? = null
        ) {
//            logger.info { "text: $text" }
            memScoped {
                val memory = with(BonkMagick) {
                    val host = call.request.headers["Host"] ?: ""
                    when(host) {
                        "bonk.nikky.moe" -> bonk(
                            bonk_jpg, bonk_jpg_size,
                            text ?: "Go to horny jail"
                        )
                        "horn.bonk.nikky.moe" -> bonk(
                            horn_jpg, horn_jpg_size,
                            text ?: "Go to horny jail"
                        )
                        "fox.bonk.nikky.moe" -> bonk(
                            fox_jpg, fox_jpg_size,
                            text ?: "Go to horny fox jail"
                        )
                        "ghost.bonk.nikky.moe" -> bonk(
                            ghost_jpg, ghost_jpg_size,
                            text ?: "sudo boo!",
                            NorthGravity,
                            "white"
                        )
                        "linus.bonk.nikky.moe" -> bonk(
                            linus_jpg, linus_jpg_size,
                            text ?: "fuck nvidia",
                            SouthGravity,
                            "white"
                        )
                        else -> bonk(
                            bonk_jpg, bonk_jpg_size,
//                            "data/bonk.jpg",
                            text ?: "Go to horny jail"
                        )
                    }
                }
                call.respondBytesWriter(ContentType.Image.JPEG) {
                    writeFully(memory, 0, memory.size32)
                }
            }
        }
        get("/{text...}") {
//            logger.info { "parameters: ${call.parameters.names()}" }
//            logger.info { "text: ${call.parameters.getAll("text")}" }
            val text = call.parameters.getAll("text")
                ?.joinToString("/")
                ?.takeIf { it.isNotEmpty() }
                ?.decodeURLQueryComponent(plusIsSpace = true)
//            val text = call.request.path()
//                .trimStart('/')
//                ?.takeIf { it != "/" }
//                ?.takeIf { it.isNotEmpty() }
//                ?.decodeURLQueryComponent(plusIsSpace = true)
//            val text = call.parameters["text"]?.decodeURLQueryComponent(plusIsSpace = true)
            respondBonk(text)
        }
        get("/") {
            respondBonk(null)
        }
//        get("/bonk") {
//            call.respondBytesWriter(ContentType.Image.JPEG) {
//                // resources.bonk
//                val pointer = resources.bonk_jpg
//                writeFully(pointer, 0L, resources.bonk_jpg_size)
////                writeFully(bytes, 0, data.bonk_jpg_size.toInt())
//            }
//        }

        get("debug/headers") {
            call.respondText {
                buildString {
                    call.request.headers.forEach { key, values ->
                        appendLine("$key = ${values.joinToString(",", "[", "]")}")
                    }
                }
            }
        }

        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/error") {
           throw Exception("test")
        }
    }

    install(StatusPages) {
//        exception<AuthenticationException> { call, cause ->
//            logger.error(cause) { "caught AuthenticationException" }
//            call.respond(HttpStatusCode.Unauthorized)
//        }
//        exception<AuthorizationException> { call, cause ->
//            logger.error(cause) { "caught AuthorizationException" }
//            call.respond(HttpStatusCode.Forbidden)
//        }
        exception<WandException> { call, cause ->
            logger.error(cause) { "caught wand exception" }
            call.respondText(text = cause.message, status = HttpStatusCode.InternalServerError)
        }
        exception<PosixException.PosixErrnoException> { call, cause ->
            logger.error(cause) { "caught posix error ${cause.errno} ${cause.message}" }
            call.respondText(text = cause.message!!, status = HttpStatusCode.InternalServerError)
        }
        exception<Exception> { call, cause ->
            logger.error(cause) { "caught ${cause::class.simpleName}" }
            call.respondText(text = cause.message!!, status = HttpStatusCode.InternalServerError)
        }
    }
}
