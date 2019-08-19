package my.company.app.web.controller.user

import io.ktor.locations.Location
import io.swagger.annotations.ApiOperation
import java.time.Instant
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebUserController.LOCATION_PREFIX)
@ApiOperation("GetUsers")
class WebGetUsersLocation

data class WebGetUsersRequestQuery(
    val name: String?,
    val email: String?,
    val createdAtBefore: Instant?
)

data class WebGetUsersResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
)
