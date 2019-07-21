package my.company.app.web.controller

import getNameFromLocation
import getPathFromLocation
import io.ktor.application.ApplicationCall
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import my.company.app.lib.controller.AbstractController
import my.company.app.web.Get
import my.company.app.web.Post
import springfox.documentation.service.Tag
import withAuthContext

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
    companion object {
        const val WEB_CONTROLLER_PREFIX = "Web"
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
        post<LOCATION> { withAuthContext { this@post.postOp() } }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    protected inline fun <reified LOCATION : Any> Routing.documentedGet(
        noinline swaggerOp: Get.() -> Unit,
        noinline getOp: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
    ) {
        pOperations += Get(
            path = getPathFromLocation(LOCATION::class),
            name = getNameFromLocation(LOCATION::class)
        ).tag(tag.name).also(swaggerOp).build()
        get<LOCATION> { withAuthContext { this@get.getOp() } }
    }
}
