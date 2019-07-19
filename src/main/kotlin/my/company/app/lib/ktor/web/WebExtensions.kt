@file:Suppress("MatchingDeclarationName")

import de.nielsfalk.ktor.swagger.Metadata
import de.nielsfalk.ktor.swagger.created
import de.nielsfalk.ktor.swagger.json
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import my.company.app.lib.eager
import my.company.app.lib.ktor.web.GlobalWebErrorHandler
import my.company.app.lib.logger
import my.company.app.lib.tx.transaction
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

data class EndpointInformation(
    val method: HttpMethod,
    val path: String,
    val requestBody: KClass<*>?,
    val responseBody: KClass<*>?
) {
    companion object Attribute {
        val key: AttributeKey<EndpointInformation> = AttributeKey(EndpointInformation::class.java.canonicalName)
    }

    override fun toString(): String {
        val str = StringBuilder(this::class.simpleName)
            .append("(")
            .append(method.value)

        requestBody?.let { str.append(" Request[").append(it.simpleName).append("]") }
        responseBody?.let { str.append(" Response[").append(it.simpleName).append("]") }

        str.append(")")

        return str.toString()
    }
}

inline fun <reified PATH : Any, reified BODY : Any, reified RESPONSE : Any> Routing.transactionalJsonPost(
    metadata: Metadata = PATH::class.simpleName!!.responds(created(json<RESPONSE>())),
    noinline errorHandler: suspend PipelineContext<Unit, ApplicationCall>.(Throwable) -> Unit = { defaultEndpointExceptionHandling(it) },
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(BODY) -> RESPONSE
) {
    post<PATH, BODY>(metadata) { _, body ->
        try {
            context.attributes.put(
                EndpointInformation.key, EndpointInformation(
                method = HttpMethod.Post,
                path = getPathFromLocation(PATH::class),
                requestBody = BODY::class,
                responseBody = RESPONSE::class
            )
            )
            val response = transaction { block(body) }
            call.respond(HttpStatusCode.Created, response)
        } catch (e: Throwable) {
            errorHandler(e)
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.getEndpointInformation(): EndpointInformation? {
    return if (context.attributes.contains(EndpointInformation.key))
        context.attributes[EndpointInformation.key]
    else null
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun getPathFromLocation(locationType: KClass<*>): String {
    val locationAnnotation = locationType.findAnnotation<Location>() ?: throw IllegalStateException("Location not found for: $locationType")
    return locationAnnotation.path
}

suspend fun PipelineContext<Unit, ApplicationCall>.defaultEndpointExceptionHandling(
    e: Throwable
) {
    try {
        val errorHandler = eager<GlobalWebErrorHandler>()
        errorHandler.handleError(this, e)
    } catch (e2: Throwable) {
        logger<GlobalWebErrorHandler>().error("Failed to handle error", e2)
        logger<GlobalWebErrorHandler>().error("Caused by:", e)
        call.respond(HttpStatusCode.InternalServerError)
    }
}
