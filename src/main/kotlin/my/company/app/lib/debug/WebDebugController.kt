package my.company.app.lib.debug

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Routing
import my.company.app.lib.controller.AbstractController
import my.company.app.web.Get
import my.company.app.web.getPathFromLocation
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ResponseMessage
import springfox.documentation.service.Tag

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebDebugController.PATH_PREFIX + "/ping")
class WebDebugPingLocation

class WebDebugController : AbstractController(Tag(CONTROLLER_NAME, "Debugging endpoints")) {
    companion object {
        const val CONTROLLER_NAME = "DebugController"
        const val PATH_PREFIX = "/debug"
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val routing: Routing.() -> Unit = {
        pOperations += Get(path = getPathFromLocation(WebDebugPingLocation::class), name = "ping")
            .res(ResponseMessage(
                HttpStatusCode.OK.value,
                "pong",
                ModelRef("string"),
                mutableMapOf(),
                mutableListOf()
            ))
            .tag(tag.name)
            .build()

        get<WebDebugPingLocation> {
            call.respond(HttpStatusCode.OK, "pong")
        }
    }
}
