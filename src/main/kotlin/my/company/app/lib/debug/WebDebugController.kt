package my.company.app.lib.debug

import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.Location
import io.ktor.routing.Routing
import my.company.app.lib.controller.AbstractController
import plainGet

@Suppress("EXPERIMENTAL_API_USAGE")
@Group(WebDebugController.CONTROLLER_NAME)
@Location(WebDebugController.PATH_PREFIX + "/ping")
class WebDebugPingLocation

class WebDebugController : AbstractController() {
    companion object {
        const val CONTROLLER_NAME = "DebugController"
        const val PATH_PREFIX = "/debug"
    }

    override val routing: Routing.() -> Unit = {
        plainGet<WebDebugPingLocation> { "pong" }
    }
}
