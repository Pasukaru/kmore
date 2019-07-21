package my.company.app.lib.controller

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Image.PNG
import io.ktor.http.ContentType.Text.CSS
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.ContentType.Text.JavaScript
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import my.company.app.lib.eager
import my.company.app.lib.swagger.SwaggerConfiguration
import java.net.URL

class SwaggerController : Controller {

    private val content = mutableMapOf<String, ResourceContent>()
    private val swaggerConfig = eager<SwaggerConfiguration>()
    private val swaggerJson: OutgoingContent.ByteArrayContent by lazy {
        val json = swaggerConfig.render().toByteArray(Charsets.UTF_8)
        object : OutgoingContent.ByteArrayContent() {
            override val contentType: ContentType = ContentType.Application.Json
            override fun bytes(): ByteArray = json
        }
    }

    override val routing: Routing.() -> Unit = {
        get("/swagger/{fileName}") {
            val fileName = call.parameters["fileName"]
            if (fileName == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val resource = when (fileName) {
                "swagger.json" -> {
                    call.respond(HttpStatusCode.OK, swaggerJson)
                    return@get
                }
                else -> ClassLoader.getSystemClassLoader().getResource("swagger/$fileName")
            }

            if (resource == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(content.getOrPut(fileName) { ResourceContent(resource) })
        }
    }
}

private val contentTypes = mapOf(
    "html" to Html,
    "css" to CSS,
    "js" to JavaScript,
    "json" to ContentType.Application.Json.withCharset(Charsets.UTF_8),
    "png" to PNG)

private class ResourceContent(val resource: URL) : OutgoingContent.ByteArrayContent() {
    private val bytes by lazy { resource.readBytes() }

    override val contentType: ContentType? by lazy {
        val extension = resource.file.substring(resource.file.lastIndexOf('.') + 1)
        contentTypes[extension] ?: Html
    }

    override val contentLength: Long? by lazy {
        bytes.size.toLong()
    }

    override fun bytes(): ByteArray = bytes
    override fun toString() = "ResourceContent \"$resource\""
}
