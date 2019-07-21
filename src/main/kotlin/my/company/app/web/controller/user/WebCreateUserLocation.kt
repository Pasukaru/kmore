package my.company.app.web.controller.user

import io.ktor.locations.Location
import io.swagger.annotations.ApiOperation
import my.company.app.lib.validation.Email
import my.company.app.lib.validation.NotBlank
import my.company.app.lib.validation.Password
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
@Location(WebUserController.LOCATION_PREFIX)
@ApiOperation("CreateUser")
class WebCreateUserLocation

data class WebCreateUserRequest(
    @field:Email
    val email: String,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    @field:Password
    val password: String
)

data class WebCreateUserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
)
