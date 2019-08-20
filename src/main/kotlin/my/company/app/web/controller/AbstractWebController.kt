package my.company.app.web.controller

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope
import my.company.app.lib.DatabaseService
import my.company.app.lib.controller.AbstractController
import my.company.app.lib.koin.lazy
import my.company.app.lib.ktor.ParameterParser
import my.company.app.web.EndpointInformation
import my.company.app.web.Get
import my.company.app.web.Post
import my.company.app.web.getNameFromLocation
import my.company.app.web.getPathFromLocation
import my.company.app.web.withAuthContext
import springfox.documentation.service.Tag

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebLocation.PATH)
class WebLocation {
    companion object {
        const val PATH = "/web"
    }
}

abstract class AbstractWebController(
    val name: String,
    val description: String = "Everything about ${name.removePrefix("Web").removeSuffix("Controller")}",
    tag: Tag = Tag(name, description)
) : AbstractController(tag) {

    protected val databaseService: DatabaseService by lazy()
    protected val parameterParser: ParameterParser by lazy()

    companion object {
        const val WEB_CONTROLLER_PREFIX = "Web"
    }

    protected inline fun <reified LOCATION> ApplicationCall.setEndpointInformation(method: HttpMethod) {
        attributes.put(EndpointInformation.key, EndpointInformation(
            method = method,
            locationClass = LOCATION::class
        ))
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    protected inline fun <reified LOCATION : Any> Routing.documentedPost(
        noinline swaggerOp: Post.() -> Unit,
        noinline postOp: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
    ) {
        pOperations += Post(
            path = getPathFromLocation(LOCATION::class),
            name = getNameFromLocation(LOCATION::class)
        ).tag(tag.name).also(swaggerOp).build()
        post<LOCATION> {
            call.setEndpointInformation<LOCATION>(HttpMethod.Post)
            withAuthContext { this@post.postOp() }
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    protected inline fun <reified LOCATION : Any> Routing.documentedGet(
        noinline swaggerOp: Get.() -> Unit,
        noinline getOp: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
    ) {
        pOperations += Get(
            path = getPathFromLocation<LOCATION>(),
            name = getNameFromLocation<LOCATION>()
        ).tag(tag.name).also(swaggerOp).build()
        get<LOCATION> {
            call.setEndpointInformation<LOCATION>(HttpMethod.Get)
            withAuthContext { this@get.getOp() }
        }
    }

    protected suspend inline fun <reified T> transaction(crossinline block: suspend CoroutineScope.() -> T): T = databaseService.transaction { block() }
    protected suspend inline fun <reified T> noTransaction(crossinline block: suspend CoroutineScope.() -> T): T = databaseService.noTransaction { block() }
}
