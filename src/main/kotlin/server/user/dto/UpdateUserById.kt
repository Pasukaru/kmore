package server.user.dto

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import server.user.UserController
import java.util.*

@KtorExperimentalLocationsAPI
@Location("${UserController.PATH_PREFIX}/{id}")
data class UserIdLocation(val id: UUID)

data class UpdateUserByIdRequest(val name: String)

data class UpdateUserByIdResponse(val id: UUID, val name: String)