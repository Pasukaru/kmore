package my.company.app.lib

import javax.validation.ConstraintViolation

abstract class ServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ValidationException(val violations: Set<ConstraintViolation<*>>) : ServiceException("{error.validation.failed}")
class InvalidJsonException(cause: Throwable? = null) : ServiceException("{error.json.invalid}", cause)
class InvalidLoginCredentialsException : ServiceException("{error.credentials.invalid}")
class InsufficientPermissionsException : ServiceException("{error.access.denied}")
class UserByEmailAlreadyExistsException : ServiceException("{error.user.email.exists}")
