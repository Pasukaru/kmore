package server.user

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.request.receive
import io.ktor.routing.Routing
import server.lib.controller.AbstractController
import server.user.dto.UpdateUserByIdResponse
import server.user.dto.UserIdLocation
import java.util.*

@KtorExperimentalLocationsAPI
class UserController : AbstractController() {
    companion object {
        const val PATH_PREFIX = "/user"
    }

    override val routing: Routing.() -> Unit = {
        jsonGet<UserIdLocation, Map<*, *>> {
            mapOf("id" to call.parameters[UserIdLocation::id.name])
        }
        jsonPut<UserIdLocation, UpdateUserByIdResponse> {
            services.user.getUserById(UUID.fromString(call.parameters[UserIdLocation::id.name]), call.receive())
        }
    }
}