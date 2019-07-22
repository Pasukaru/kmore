package my.company.app.business_logic

import com.nhaarman.mockitokotlin2.any
import my.company.app.business_logic.session.LoginAction
import my.company.app.business_logic.session.LoginRequest
import my.company.app.db.newSession
import my.company.app.db.newUser
import my.company.app.lib.PasswordHelper
import my.company.app.test.declareMock
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class LoginActionTest : AbstractActionTest() {

    private lateinit var passwordHelper: PasswordHelper

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        passwordHelper = declareMock()
    }

    private data class Context(
        val request: LoginRequest,
        val mockedUser: UserRecord,
        val mockedSession: SessionRecord
    )

    private suspend fun validContext(): Context {
        val mockedUser = newUser(
            email = "derp",
            lastName = "derp",
            firstName = "derp",
            password = "{hashed-password}"
        )

        val mockedSession = newSession(
            userId = mockedUser.id
        )

        Mockito.doReturn(true).`when`(passwordHelper).checkPassword(any(), any())
        Mockito.doReturn(mockedUser).`when`(repo.user).findByEmailIgnoringCase(any())
        Mockito.doReturn(mockedSession).`when`(repo.session).insert(any())

        return Context(
            request = LoginRequest(mockedUser.email, mockedUser.password),
            mockedUser = mockedUser,
            mockedSession = mockedSession
        )
    }

    @Test
    fun canLoginWithValidCredentials() = actionTest {
        val ctx = validContext()

        val response = LoginAction().execute(ctx.request)
    }
}
