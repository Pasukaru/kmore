package my.company.app.lib.controller

import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.lib.inject
import my.company.app.lib.logger

abstract class AbstractController : Controller {

    protected val logger = this::class.logger()

    protected val userActions: UserActions by inject()
    protected val sessionActions: SessionActions by inject()
}
