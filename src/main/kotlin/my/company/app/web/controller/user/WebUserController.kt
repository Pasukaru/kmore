package my.company.app.web.controller.user

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import my.company.app.web.controller.AbstractWebController
import my.company.app.web.controller.WebLocation

class WebUserController : AbstractWebController(
    name = CONTROLLER_NAME
) {
    companion object {
        const val CONTROLLER_NAME = "${WEB_CONTROLLER_PREFIX}UserController"
        const val LOCATION_PREFIX = "${WebLocation.PATH}/user"
    }

    private val mapper = WebUserMapper()

    override val routing: Routing.() -> Unit = {
        documentedPost<WebCreateUserLocation>({ req<WebCreateUserRequest>().res<WebCreateUserResponse>() }) {
            val req = mapper.req(validate(call.receive<WebCreateUserRequest>()))
            val res = mapper.res(userActions.createUser.execute(req))
            call.respond(HttpStatusCode.Created, res)
        }
        documentedGet<WebGetUsersLocation>({ resList<WebGetUsersResponse>().query<WebGetUsersRequestQuery>() }) {
            val query = parameterParser.parse<WebGetUsersRequestQuery>(call.parameters)
            val res = mapper.res(userActions.getUsers.execute(mapper.req(query)))
            call.respond(HttpStatusCode.OK, res)
        }
    }
}
