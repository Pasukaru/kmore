package my.company.app.lib.controller

import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.lib.inject
import my.company.app.lib.logger
import my.company.app.lib.validation.ValidationException
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.Tag
import javax.validation.Validator

abstract class AbstractController(
    val tag: Tag
) : Controller {

    protected val logger = this::class.logger()

    protected val validator: Validator by inject()

    protected val userActions: UserActions by inject()
    protected val sessionActions: SessionActions by inject()

    protected val pOperations = mutableListOf<ApiDescription>()
    val operations get() = pOperations as List<ApiDescription>

    protected fun <T : Any> validate(obj: T): T {
        val result = validator.validate(obj)
        if (result.isEmpty()) return obj
        throw ValidationException(result)
    }
}
