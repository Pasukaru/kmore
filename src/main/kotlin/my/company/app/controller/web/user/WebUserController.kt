package my.company.app.controller.web.user

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import my.company.app.business_logic.user.CreateUserAction
import my.company.app.business_logic.user.UserActions
import my.company.app.lib.controller.AbstractController
import java.util.*

class WebUserController : AbstractController() {
    companion object {
        const val PATH_PREFIX = "/user"
    }

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

    suspend fun createUser(pipeline: PipelineContext<Unit, ApplicationCall>) {
        val body = pipeline.call.receive<WebCreateUserRequest>()
        val result = UserActions.createUser.execute(
            CreateUserAction.Request(
                email = body.email,
                firstName = body.firstName,
                lastName = body.lastName,
                passwordClean = body.password
            )
        )
        WebCreateUserResponse(
            id = result.id,
            email = result.email,
            firstName = result.firstName,
            lastName = result.lastName
        )
    }

    override val routing: Routing.() -> Unit = {
        jsonPost(PATH_PREFIX) { createUser(this) }
    }
}
