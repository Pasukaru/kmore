package my.company.app.lib.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotBlankValidator::class])
annotation class NotBlank(
    val message: String = "{validation.not.blank}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NotBlankValidator : ConstraintValidator<NotBlank, String> {
    companion object {
        val INSTANCE = NotBlankValidator()
        fun isValid(value: String): Boolean = INSTANCE.isValid(value, null)
    }

    override fun initialize(p0: NotBlank?) {}
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return when (value) {
            null -> true
            else -> !value.isBlank()
        }
    }
}
