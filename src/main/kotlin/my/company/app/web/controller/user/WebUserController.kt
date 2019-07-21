package my.company.app.web.controller.user

import de.nielsfalk.ktor.swagger.json
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.responds
import io.ktor.routing.Routing
import my.company.app.web.controller.AbstractWebController
import my.company.app.web.controller.WebLocation
import transactionalJsonGetList
import transactionalJsonPost

class WebUserController : AbstractWebController() {
    companion object {
        const val CONTROLLER_NAME = "${CONTROLLER_PREFIX}UserController"
        const val LOCATION_PREFIX = "${WebLocation.PATH}/user"
    }

    private val mapper = WebUserMapper()

    override val routing: Routing.() -> Unit = {
        transactionalJsonPost<WebCreateUserLocation, WebCreateUserRequest, WebCreateUserResponse> {
            mapper.res(userActions.createUser.execute(mapper.req(validate(it))))
        }

        transactionalJsonGetList<WebGetUsersLocation, WebGetUsersResponse>(
            metadata = "getAll".responds(ok(json<WebGetUsersResponse>()))
        ) {
            mapper.res(userActions.getUsers.execute(Unit))
        }
    }
}
