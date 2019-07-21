package my.company.app.web.controller.session

import io.ktor.locations.Location
import io.swagger.annotations.ApiOperation
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebSessionController.LOCATION_PREFIX)
@ApiOperation("Login")
object WebLoginLocation

data class WebLoginRequest(
    val email: String,
    val password: String
)

data class WebLoginResponse(
    val id: UUID
)
