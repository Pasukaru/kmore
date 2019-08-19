package my.company.app.business_logic.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import my.company.app.business_logic.AbstractActionTest
import my.company.app.lib.Faker
import my.company.app.test.captor
import my.company.app.test.singleValue
import my.company.jooq.tables.records.UserRecord
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.time.Instant

class GetUsersActionTest : AbstractActionTest() {

    private data class Context(
        val filter: GetUsersFilter,
        val filterCaptor: ArgumentCaptor<GetUsersFilter>,
        val users: List<UserRecord>
    )

    private suspend fun validContext(): Context {
        val users = (0 until 10).map { fixtures.user() }

        val filterCaptor = captor<GetUsersFilter>()
        Mockito.doReturn(users).`when`(repo.user).findByFilter(capture(filterCaptor))

        return Context(
            filter = GetUsersFilter(
                email = null,
                name = null,
                createdAtBefore = null
            ),
            filterCaptor = filterCaptor,
            users = users.map { user -> user.copy().also { it.id = user.id } }
        )
    }

    @Test
    fun canGetUsers() = actionTest {
        val ctx = validContext()
        val response = GetUsersAction().execute(ctx.filter)
        ctx.users.forEachIndexed { i, r -> assertThat(response[i]).isEqualTo(r) }
        Mockito.verify(mockedAuthorizationService).expectPermission("USERS_CAN_READ")
        Mockito.verify(repo.user).findByFilter(any())
        assertThat(ctx.filterCaptor.singleValue).isEqualTo(ctx.filter)
    }

    @Test
    fun canGetUsersWithFilter() = actionTest {
        val ctx = validContext()
        val filter = ctx.filter.copy(
            email = Faker.internet().emailAddress(),
            name = Faker.artist().name(),
            createdAtBefore = Instant.now()
        )
        val response = GetUsersAction().execute(filter)
        ctx.users.forEachIndexed { i, r -> assertThat(response[i]).isEqualTo(r) }
        Mockito.verify(mockedAuthorizationService).expectPermission("USERS_CAN_READ")
        Mockito.verify(repo.user).findByFilter(any())
        assertThat(ctx.filterCaptor.singleValue).isEqualTo(filter)
    }
}
