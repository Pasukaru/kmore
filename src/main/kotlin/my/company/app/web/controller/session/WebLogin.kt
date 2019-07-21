package my.company.app.web.controller.session

import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.Location
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Group(WebSessionController.CONTROLLER_NAME)
@Location(WebSessionController.LOCATION_PREFIX)
object WebLoginLocation

data class WebLoginRequest(
    val email: String,
    val password: String
)

data class WebLoginResponse(
    val id: UUID
)
