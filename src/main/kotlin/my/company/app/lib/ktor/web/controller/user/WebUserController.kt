package my.company.app.lib.ktor.web.controller.user

import io.ktor.routing.Routing
import my.company.app.lib.controller.AbstractController
import transactionalJsonPost

class WebUserController : AbstractController() {
    companion object {
        const val CONTROLLER_NAME = "WebUserController"
        const val PATH_PREFIX = "/user"
    }

    private val mapper = WebUserMapper()

    override val routing: Routing.() -> Unit = {
        transactionalJsonPost<WebCreateUserPath, WebCreateUserRequest, WebCreateUserResponse> {
            mapper.res(userActions.createUser.execute(mapper.req(it)))
        }
    }
}
