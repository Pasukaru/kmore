package my.company.app.lib.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class Password(
    val message: String = "{validation.password.invalid}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordValidator : ConstraintValidator<Password, String> {
    companion object {
        private val PASSWORD_PATTERN = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}\$")
        val INSTANCE = PasswordValidator()
        fun isValid(value: String): Boolean = INSTANCE.isValid(value, null)
    }

    override fun initialize(p0: Password?) {}
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        value ?: return true
        return PASSWORD_PATTERN.matches(value)
    }
}
