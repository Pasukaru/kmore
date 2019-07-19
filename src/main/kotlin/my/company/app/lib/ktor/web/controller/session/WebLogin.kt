package my.company.app.lib.ktor.web.controller.session

import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.Location
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Group(WebSessionController.CONTROLLER_NAME)
@Location(WebSessionController.PATH_PREFIX)
object WebLoginPath

data class WebLoginRequest(
    val email: String,
    val password: String
)

data class WebLoginResponse(
    val id: UUID
)
