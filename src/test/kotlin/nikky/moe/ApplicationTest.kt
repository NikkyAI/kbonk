package nikky.moe

import io.ktor.http.*
import io.ktor.server.plugins.*
import org.slf4j.event.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlin.test.*
import io.ktor.server.testing.*
import nikky.moe.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World!", response.content)
            }
        }
    }
}