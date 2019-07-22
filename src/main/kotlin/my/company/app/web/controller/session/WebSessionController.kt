package my.company.app.web.controller.session

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import my.company.app.lib.transaction
import my.company.app.web.controller.AbstractWebController
import my.company.app.web.controller.WebLocation

class WebSessionController : AbstractWebController(
    name = CONTROLLER_NAME
) {
    companion object {
        const val CONTROLLER_NAME = "${WEB_CONTROLLER_PREFIX}SessionController"
        const val LOCATION_PREFIX = "${WebLocation.PATH}/session"
    }

    private val mapper = WebSessionMapper()

    override val routing: Routing.() -> Unit = {
        documentedPost<WebLoginLocation>({ req<WebLoginRequest>().res<WebLoginResponse>() }) {
            val request = mapper.req(validate(call.receive()))
            val response = mapper.res(transaction { sessionActions.login.execute(request) })
            call.respond(HttpStatusCode.Created, response)
        }
    }
}
