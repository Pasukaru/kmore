package my.company.app.controller.user

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.request.receive
import io.ktor.routing.Routing
import my.company.app.business_logic.user.dto.UpdateUserByIdAction
import my.company.app.controller.user.dto.UpdateUserByIdRequest
import my.company.app.controller.user.dto.UpdateUserByIdResponse
import my.company.app.controller.user.dto.UserIdLocation
import my.company.app.lib.controller.AbstractController
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
            val body = call.receive<UpdateUserByIdRequest>()
            val action = UpdateUserByIdAction(
                id = UUID.fromString(call.parameters[UserIdLocation::id.name]),
                name = body.name
            )
            services.user.updateUserById(action).let { result ->
                UpdateUserByIdResponse(
                    id = result.id,
                    name = result.name
                )
            }
        }
    }
}