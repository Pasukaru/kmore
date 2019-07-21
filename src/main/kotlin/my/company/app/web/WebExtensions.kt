@file:Suppress("MatchingDeclarationName")

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.withContext
import my.company.app.web.auth.WebSessionPrincipal
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

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

fun PipelineContext<Unit, ApplicationCall>.getEndpointInformation(): EndpointInformation? {
    if (!context.attributes.contains(EndpointInformation.key)) return null
    return context.attributes[EndpointInformation.key]
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun getPathFromLocation(locationType: KClass<*>): String {
    val locationAnnotation = locationType.findAnnotation<Location>() ?: throw IllegalStateException("Location not found for: $locationType")
    return locationAnnotation.path
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun getNameFromLocation(locationType: KClass<*>): String {
    return locationType.simpleName!!.removePrefix("Web").removeSuffix("Location")
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> mapParameters(parameters: Parameters): T {
    if (T::class == Unit::class) return Unit as T
    throw NotImplementedError()
}
