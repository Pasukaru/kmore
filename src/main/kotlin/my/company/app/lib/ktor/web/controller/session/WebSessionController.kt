package my.company.app.lib.ktor.web.controller.session

import io.ktor.routing.Routing
import my.company.app.lib.controller.AbstractController
import transactionalJsonPost

class WebSessionController : AbstractController() {
    companion object {
        const val CONTROLLER_NAME = "WebSessionController"
        const val PATH_PREFIX = "/session"
    }

    private val mapper = WebSessionMapper()

    override val routing: Routing.() -> Unit = {
        transactionalJsonPost<WebLoginPath, WebLoginRequest, WebLoginResponse> {
            mapper.res(sessionActions.login.execute(mapper.req(it)))
        }
    }
}
