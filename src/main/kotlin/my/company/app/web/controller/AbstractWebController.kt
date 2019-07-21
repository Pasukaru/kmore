package my.company.app.web.controller

import io.ktor.locations.Location
import my.company.app.lib.controller.AbstractController

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebLocation.PATH)
class WebLocation {
    companion object {
        const val PATH = "/web"
    }
}

abstract class AbstractWebController : AbstractController() {
    companion object {
        const val CONTROLLER_PREFIX = "Web"
    }
}
