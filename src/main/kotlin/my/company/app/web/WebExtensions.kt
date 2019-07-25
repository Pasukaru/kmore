package my.company.app.web

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.locations.Location
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.withContext
import my.company.app.web.auth.WebSessionPrincipal
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

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

inline fun <reified LOCATION> getPathFromLocation() = getPathFromLocation(LOCATION::class)

@Suppress("EXPERIMENTAL_API_USAGE")
fun getPathFromLocation(locationType: KClass<*>): String {
    val locationAnnotation = locationType.findAnnotation<Location>() ?: throw IllegalStateException("Location not found for: $locationType")
    return locationAnnotation.path
}

@Suppress("EXPERIMENTAL_API_USAGE")
inline fun <reified LOCATION> getNameFromLocation() = getNameFromLocation(LOCATION::class)

@Suppress("EXPERIMENTAL_API_USAGE")
fun getNameFromLocation(locationType: KClass<*>): String {
    return locationType.simpleName!!.removePrefix("Web").removeSuffix("Location")
}
