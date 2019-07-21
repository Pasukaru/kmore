package my.company.app.web.controller.session

import io.ktor.routing.Routing
import my.company.app.web.controller.AbstractWebController
import my.company.app.web.controller.WebLocation
import transactionalJsonPost

class WebSessionController : AbstractWebController() {
    companion object {
        const val CONTROLLER_NAME = "${CONTROLLER_PREFIX}SessionController"
        const val LOCATION_PREFIX = "${WebLocation.PATH}/session"
    }

    private val mapper = WebSessionMapper()

    override val routing: Routing.() -> Unit = {
        transactionalJsonPost<WebLoginLocation, WebLoginRequest, WebLoginResponse> {
            mapper.res(sessionActions.login.execute(mapper.req(validate(it))))
        }
    }
}
