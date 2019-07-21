package my.company.app.lib.controller

import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.lib.inject
import my.company.app.lib.logger
import my.company.app.lib.validation.ValidationService
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.Tag

abstract class AbstractController(
    val tag: Tag
) : Controller {

    protected val logger = this::class.logger()

    protected val validator: ValidationService by inject()

    protected val userActions: UserActions by inject()
    protected val sessionActions: SessionActions by inject()

    protected val pOperations = mutableListOf<ApiDescription>()
    val operations get() = pOperations as List<ApiDescription>

    protected fun <T : Any> validate(obj: T): T = validator.validate(obj)
}
