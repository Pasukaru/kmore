package my.company.app.lib

import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import java.lang.annotation.ElementType
import javax.validation.ConstraintViolation
import javax.validation.Path

abstract class ServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

open class ValidationException(val violations: Set<ConstraintViolation<*>>) : ServiceException("{error.validation.failed}")
class InvalidInstantException(violations: Set<ConstraintViolation<*>>) : ValidationException(violations) {
    constructor(value: Any, propertyPath: Path) : this(setOf(ConstraintViolationImpl.forParameterValidation(
        "{error.timestamp.format.invalid}",
        mutableMapOf<String?, Any?>(),
        mutableMapOf<String?, Any?>(),
        "",
        null,
        null,
        null,
        value,
        propertyPath,
        null,
        ElementType.FIELD,
        emptyArray<Any?>(),
        null
    )))
}
class InvalidJsonException : ServiceException("{error.json.invalid}")
class InvalidLoginCredentialsException : ServiceException("{error.credentials.invalid}")
class InsufficientPermissionsException : ServiceException("{error.access.denied}")
class UserByEmailAlreadyExistsException : ServiceException("{error.user.email.exists}")
