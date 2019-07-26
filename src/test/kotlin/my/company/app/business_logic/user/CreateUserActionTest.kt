package my.company.app.business_logic.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import my.company.app.business_logic.AbstractActionTest
import my.company.app.lib.Faker
import my.company.app.lib.PasswordHelper
import my.company.app.lib.UserByEmailAlreadyExistsException
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.expectAllChanged
import my.company.app.test.expectEmailValidation
import my.company.app.test.expectException
import my.company.app.test.expectNotBlankValidation
import my.company.app.test.expectPasswordValidation
import my.company.app.test.singleValue
import my.company.jooq.tables.records.UserRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.UUID

class CreateUserActionTest : AbstractActionTest() {

    private lateinit var passwordHelper: PasswordHelper

    @BeforeEach
    override fun beforeEach() = super.beforeEach().also {
        passwordHelper = declareMock()
    }

    private data class Context(
        val request: CreateUserRequest,
        val hashedPassword: String,
        val createdUser: ArgumentCaptor<UserRecord>
    )

    private suspend fun validContext(): Context {
        val request = CreateUserRequest(
            email = Faker.internet().emailAddress(),
            firstName = Faker.name().firstName(),
            lastName = Faker.name().lastName(),
            passwordClean = Faker.internet().password()
        )

        val createdUser = captor<UserRecord>()

        val hashedPassword = Faker.crypto().sha1()

        Mockito.doReturn(false).`when`(repo.user).existsByEmailIgnoringCase(request.email)
        Mockito.doReturn(hashedPassword).`when`(passwordHelper).hashPassword(any())
        Mockito.doAnswer {
            val user = it.arguments.first() as UserRecord
            user.createdAt = mockedTimeService.now()
            user
        }.`when`(repo.user).insert(capture(createdUser))

        return Context(
            request = request,
            hashedPassword = hashedPassword,
            createdUser = createdUser
        )
    }

    @Test
    fun requestIsValidated() {
        expectEmailValidation(CreateUserRequest::email)
        expectNotBlankValidation(CreateUserRequest::firstName)
        expectNotBlankValidation(CreateUserRequest::lastName)
        expectPasswordValidation(CreateUserRequest::passwordClean)
    }

    @Test
    fun canCreateUser() = actionTest {
        val ctx = validContext()

        val response = CreateUserAction().execute(ctx.request)

        val user = ctx.createdUser.singleValue
        assertThat(response).isSameAs(user)
        assertThat(user.id).isInstanceOf(UUID::class)
        assertThat(user.email).isEqualTo(ctx.request.email)
        assertThat(user.firstName).isEqualTo(ctx.request.firstName)
        assertThat(user.lastName).isEqualTo(ctx.request.lastName)
        assertThat(user.password).isEqualTo(ctx.hashedPassword)
        assertThat(user.createdAt).isEqualTo(mockedTimeService.now())
        assertThat(user.updatedAt).isNull()
        user.expectAllChanged()

        Mockito.verify(validationService).validate(ctx.request)
        Mockito.verify(repo.user).existsByEmailIgnoringCase(ctx.request.email)
        Mockito.verify(passwordHelper).hashPassword(ctx.request.passwordClean)
        Mockito.verify(repo.user).insert(user)
        Mockito.verify(spiedModelGenerator).user(
            email = ctx.request.email,
            firstName = ctx.request.firstName,
            lastName = ctx.request.lastName,
            password = ctx.hashedPassword
        )
    }

    @Test
    fun throwsExceptionWhenUserExistsCreateUser() = actionTest {
        val ctx = validContext()
        Mockito.doReturn(true).`when`(repo.user).existsByEmailIgnoringCase(ctx.request.email)
        expectException<UserByEmailAlreadyExistsException> { CreateUserAction().execute(ctx.request) }
    }
}
