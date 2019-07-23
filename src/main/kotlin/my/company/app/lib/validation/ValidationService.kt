package my.company.app.lib.validation

import my.company.app.lib.ValidationException
import my.company.app.lib.lazy
import javax.validation.Validator

class ValidationService {
    private val validator: Validator by lazy()

    fun <T : Any> validate(obj: T): T {
        val result = validator.validate(obj)
        if (result.isEmpty()) return obj
        throw ValidationException(result)
    }
}
