package my.company.app.lib.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EmailValidator::class])
annotation class Email(
    val message: String = "{validation.email.invalid}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class EmailValidator : ConstraintValidator<Email, String> {
    companion object {
        private val HIBERNATE_EMAIL_VALIDATOR = org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator()
        val INSTANCE = EmailValidator()
        fun isValid(value: String): Boolean = INSTANCE.isValid(value, null)
    }

    override fun initialize(p0: Email?) {}
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return when {
            value == null -> true
            value.isBlank() -> false
            else -> HIBERNATE_EMAIL_VALIDATOR.isValid(value, context)
        }
    }
}
