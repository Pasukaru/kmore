package my.company.app.lib.validation

import my.company.app.lib.ServiceException
import javax.validation.ConstraintViolation

class ValidationException(val violations: Set<ConstraintViolation<*>>) : ServiceException("{validation.failed}")
