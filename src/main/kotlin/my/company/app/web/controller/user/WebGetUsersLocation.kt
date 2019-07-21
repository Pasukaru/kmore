package my.company.app.web.controller.user

import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.Location
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Group(WebUserController.CONTROLLER_NAME)
@Location(WebUserController.LOCATION_PREFIX)
class WebGetUsersLocation

data class WebGetUsersResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
)
