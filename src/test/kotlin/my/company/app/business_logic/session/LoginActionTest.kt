package my.company.app.business_logic.session

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import my.company.app.business_logic.AbstractActionTest
import my.company.app.generated.jooq.tables.records.SessionRecord
import my.company.app.generated.jooq.tables.records.UserRecord
import my.company.app.lib.InvalidLoginCredentialsException
import my.company.app.lib.PasswordHelper
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.expectAllChanged
import my.company.app.test.expectAllUnchanged
import my.company.app.test.expectException
import my.company.app.test.singleValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.UUID

class LoginActionTest : AbstractActionTest() {

    private lateinit var passwordHelper: PasswordHelper

    @BeforeEach
    override fun beforeEach() = super.beforeEach().also {
        passwordHelper = declareMock()
    }

    private data class Context(
        val request: LoginRequest,
        val mockedUser: UserRecord,
        val createdSession: ArgumentCaptor<SessionRecord>
    )

    private suspend fun validContext(): Context {
        val mockedUser = fixtures.user()
        val createdSession = captor<SessionRecord>()

        Mockito.doReturn(mockedUser).`when`(repo.user).findByEmailIgnoringCase(any())
        Mockito.doReturn(true).`when`(passwordHelper).checkPassword(any(), any())
        Mockito.doAnswer {
            val session = it.arguments.first() as SessionRecord
            session.createdAt = mockedTimeService.now()
            session
        }.`when`(repo.session).insert(capture(createdSession))

        return Context(
            request = LoginRequest(mockedUser.email, mockedUser.password),
            mockedUser = mockedUser,
            createdSession = createdSession
        )
    }

    @Test
    fun canLoginWithValidCredentials() = actionTest {
        val ctx = validContext()
        val response = LoginAction().execute(ctx.request)

        ctx.createdSession.singleValue.also { session ->
            assertThat(response).isSameAs(session)
            assertThat(session.id).isInstanceOf(UUID::class)
            assertThat(session.userId).isEqualTo(ctx.mockedUser.id)
            assertThat(session.createdAt).isEqualTo(mockedTimeService.now())
            assertThat(session.updatedAt).isNull()
            session.expectAllChanged()
        }

        ctx.mockedUser.expectAllUnchanged()
        expectTransaction()

        Mockito.verify(repo.user).findByEmailIgnoringCase(ctx.request.email)
        Mockito.verify(passwordHelper).checkPassword(ctx.mockedUser.password, ctx.request.passwordClean)
        Mockito.verify(repo.session).insert(response)
    }

    @Test
    fun throwsInvalidLoginCredentialsExceptionWhenCredentialsAreInvalid() = actionTest {
        val ctx = validContext()
        Mockito.doReturn(false).`when`(passwordHelper).checkPassword(any(), any())
        expectException<InvalidLoginCredentialsException> { LoginAction().execute(ctx.request) }
    }

    @Test
    fun throwsInvalidLoginCredentialsExceptionWhenUserDoesNotExist() = actionTest {
        val ctx = validContext()
        Mockito.doReturn(null).`when`(repo.user).findByEmailIgnoringCase(any())
        expectException<InvalidLoginCredentialsException> { LoginAction().execute(ctx.request) }
    }
}
