package my.company.app.business_logic.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import my.company.app.business_logic.AbstractActionTest
import my.company.jooq.tables.records.UserRecord
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class GetUsersActionTest : AbstractActionTest() {

    private data class Context(
        val users: List<UserRecord>
    )

    private suspend fun validContext(): Context {
        val users = (0 until 10).map { fixtures.user() }

        Mockito.doReturn(users).`when`(repo.user).findAll()

        return Context(
            users = users.map { user -> user.copy().also { it.id = user.id } }
        )
    }

    @Test
    fun canGetCreateUser() = actionTest {
        val ctx = validContext()
        val response = GetUsersAction().execute(Unit)
        ctx.users.forEachIndexed { i, r -> assertThat(response[i]).isEqualTo(r) }
        Mockito.verify(mockedAuthorizationService).expectPermission("USERS_CAN_READ")
        Mockito.verify(repo.user).findAll()
    }
}
