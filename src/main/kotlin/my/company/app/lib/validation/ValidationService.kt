package my.company.app.lib.validation

import my.company.app.lib.inject
import javax.validation.Validator

class ValidationService {
    private val validator: Validator by inject()

    fun <T : Any> validate(obj: T): T {
        val result = validator.validate(obj)
        if (result.isEmpty()) return obj
        throw ValidationException(result)
    }
}
