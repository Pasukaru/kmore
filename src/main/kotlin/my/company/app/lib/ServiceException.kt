package my.company.app.lib

abstract class ServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class InsufficientPermissionsException : ServiceException("{error.access.denied}")

class UserByEmailAlreadyExistsException : ServiceException("{error.user.email.exists}")
