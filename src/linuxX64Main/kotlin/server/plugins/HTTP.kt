package nikky.moe.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureHTTP() {
    install(CORS) {
        methods += HttpMethod.Get
        methods += HttpMethod.Options
        methods += HttpMethod.Put
        methods += HttpMethod.Delete
        methods += HttpMethod.Patch

        headers += HttpHeaders.Authorization

        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(CachingHeaders) {
        options { applicationCall, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 3600))
                ContentType.Image.JPEG -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 3600))
                else -> null
            }
        }
    }
}
