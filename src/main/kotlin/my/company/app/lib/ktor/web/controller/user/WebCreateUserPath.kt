package my.company.app.lib.ktor.web.controller.user

import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.Location
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Group(WebUserController.CONTROLLER_NAME)
@Location(WebUserController.PATH_PREFIX)
class WebCreateUserPath

data class WebCreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

data class WebCreateUserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
)
