package my.company.app.lib.validation

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import my.company.app.lib.Faker
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import javax.validation.ConstraintValidator

@Execution(ExecutionMode.CONCURRENT)
class ValidatorTest {

    @Test
    fun emailValidatorWorks() = with(EmailValidator.INSTANCE) {
        expectInvalid("")
        expectInvalid("not-an-email")
        expectInvalid("almost-an-email@@@@")
        expectInvalid(" no spaces allowed @ something . com")
        expectInvalid(" " + Faker.internet().emailAddress())
        repeat(100) {
            expectValid(Faker.internet().emailAddress())
        }
    }

    @Test
    fun notBlankValidatorWorks() = with(NotBlankValidator.INSTANCE) {
        expectInvalid("")
        expectInvalid(" ")
        expectInvalid("\t")
        expectInvalid("\n")
        expectInvalid("\r")
        expectInvalid("\t\r\n")
        expectValid("1")
    }

    @Test
    fun passwordValidatorWorks() = with(PasswordValidator.INSTANCE) {
        expectInvalid("@!A4c6A")
        expectInvalid("aaa2123vvc")
        expectInvalid("&&!&!§%/%")
        expectInvalid("a!dfg%c")
        expectValid("@!A4c6A467")
    }

    private fun <T : Any> ConstraintValidator<*, T>.expectValid(value: T?) {
        assertThat(isValid(value, null), "Expected validation for $value with ${this::class} to succeed, but it failed.").isTrue()
    }

    private fun <T : Any> ConstraintValidator<*, T>.expectInvalid(value: T?) {
        assertThat(isValid(value, null), "Expected validation for $value with ${this::class} to fail, but it didn't.").isFalse()
    }
}
