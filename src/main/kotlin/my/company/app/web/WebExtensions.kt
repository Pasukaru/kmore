@file:Suppress("MatchingDeclarationName")

import de.nielsfalk.ktor.swagger.Metadata
import de.nielsfalk.ktor.swagger.created
import de.nielsfalk.ktor.swagger.get
import de.nielsfalk.ktor.swagger.json
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.withContext
import my.company.app.PackageNoOp
import my.company.app.lib.eager
import my.company.app.lib.logger
import my.company.app.lib.plain
import my.company.app.lib.tx.transaction
import my.company.app.web.auth.WebSessionPrincipal
import javax.validation.Validator
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.system.measureTimeMillis

data class EndpointInformation(
    val method: HttpMethod,
    val locationClass: KClass<*>,
    val location: String,
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

suspend inline fun <T> PipelineContext<*, ApplicationCall>.withAuthContext(
    crossinline block: suspend PipelineContext<*, ApplicationCall>.() -> T
): T {
    val authContext = call.authentication.principal as? WebSessionPrincipal
    return if (authContext != null) {
        withContext(authContext) { block() }
    } else {
        block()
    }
}

inline fun <reified PATH : Any, reified BODY : Any, reified RESPONSE : Any> Routing.transactionalJsonPost(
    metadata: Metadata = PATH::class.simpleName!!.responds(created(json<RESPONSE>())),
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(BODY) -> RESPONSE
) {
    post<PATH, BODY>(metadata) { _, body ->
            eager<Validator>().validate(body)
            context.attributes.put(
                EndpointInformation.key, EndpointInformation(
                    method = HttpMethod.Post,
                    locationClass = PATH::class,
                    location = getLocationFromClass(PATH::class),
                    requestBody = BODY::class,
                    responseBody = RESPONSE::class
                )
            )

            val response = withAuthContext { transaction { this@post.block(body) } }
            call.respond(HttpStatusCode.Created, response)
    }
}

inline fun <reified PATH : Any, reified RESPONSE : Any> Routing.transactionalJsonGet(
    metadata: Metadata = PATH::class.simpleName!!.responds(created(json<RESPONSE>())),
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> RESPONSE
) {
    get<PATH>(metadata) {
        context.attributes.put(
            EndpointInformation.key, EndpointInformation(
                method = HttpMethod.Post,
                locationClass = PATH::class,
                location = getLocationFromClass(PATH::class),
                requestBody = null,
                responseBody = RESPONSE::class
            )
        )

        val response = withAuthContext { transaction { this@get.block() } }
        call.respond(HttpStatusCode.Created, response)
    }
}

inline fun <reified PATH : Any, reified RESPONSE : Any> Routing.transactionalJsonGetList(
    metadata: Metadata = PATH::class.simpleName!!.responds(created(json<RESPONSE>())),
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> List<RESPONSE>
) {
    get<PATH>(metadata) {
        context.attributes.put(
            EndpointInformation.key, EndpointInformation(
                method = HttpMethod.Post,
                locationClass = PATH::class,
                location = getLocationFromClass(PATH::class),
                requestBody = null,
                responseBody = RESPONSE::class
            )
        )

        val response = withAuthContext { transaction { this@get.block() } }
        call.respond(HttpStatusCode.Created, response)
    }
}

inline fun <reified PATH : Any> Routing.plainGet(
    metadata: Metadata = PATH::class.simpleName!!.responds(ok(plain())),
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> String
) {
    get<PATH>(metadata) {
        val time = measureTimeMillis {
            context.attributes.put(
                EndpointInformation.key, EndpointInformation(
                    method = HttpMethod.Post,
                    locationClass = PATH::class,
                    location = getLocationFromClass(PATH::class),
                    requestBody = null,
                    responseBody = String::class
                )
            )
            val response = withAuthContext { this@get.block() }
            call.respond(HttpStatusCode.Created, response)
        }
        logger<PackageNoOp>().debug("Request completed in ${time}ms")
    }
}

fun PipelineContext<Unit, ApplicationCall>.getEndpointInformation(): EndpointInformation? {
    if (!context.attributes.contains(EndpointInformation.key)) return null
    return context.attributes[EndpointInformation.key]
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun getLocationFromClass(locationType: KClass<*>): String {
    val locationAnnotation = locationType.findAnnotation<Location>() ?: throw IllegalStateException("Location not found for: $locationType")
    return locationAnnotation.path
}

