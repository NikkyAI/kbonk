package server.plugins

import BonkMagick
import WandException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.cinterop.memScoped
import logging.getLogger
import mu.KotlinLogging

fun Application.configureRouting() {
    val logger = getLogger("Server")
    install(ContentNegotiation) {
        json()
    }
//    install(ShutDownUrl.ApplicationCallFeature) {
//        // The URL that will be intercepted
//        shutDownUrl = "/ktor/application/shutdown"
//        // A function that will be executed to get the exit code of the process
//        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
//    }
    routing {
        get("/{text?}") {
            val text = call.parameters["text"]?.decodeURLQueryComponent(plusIsSpace = true) ?: "Go to horny jail"
            memScoped {
                val memory = with(BonkMagick) {
                    bonk("data/bonk.jpg", text)
                }
                call.respondBytesWriter(ContentType.Image.JPEG) {
                    writeFully(memory, 0, memory.size32)
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
        exception<Exception> { call, cause: Throwable ->
            logger.error(cause) { "caught ${cause::class.simpleName}" }
            call.respondText(text = cause.message!!, status = HttpStatusCode.InternalServerError)
        }
    }
}
