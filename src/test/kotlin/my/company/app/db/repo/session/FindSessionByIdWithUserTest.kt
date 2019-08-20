package my.company.app.db.repo.session

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import my.company.app.db.repo.AbstractRepositoryTest
import my.company.app.test.expectNotNull
import my.company.app.test.expectNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant
import java.util.UUID

class FindSessionByIdWithUserTest : AbstractRepositoryTest() {
    @Test
    fun canFindBySessionId() = queryTest {
        val instant = Instant.now()

        Mockito.doReturn(instant).`when`(mockedTimeService).now()
        val userA = fixtures.user()
        val userB = fixtures.user()

        Mockito.doReturn(instant.plusSeconds(1)).`when`(mockedTimeService).now()
        val sessionA = fixtures.session(userId = userA.id)
        fixtures.session(userId = userB.id)

        val (session, user) = repo.session.getSessionByIdWithUser(sessionA.id).expectNotNull()
        assertThat(session.changed()).isFalse()
        assertThat(session).isEqualTo(sessionA)

        assertThat(user.changed()).isFalse()
        assertThat(user).isEqualTo(userA)

        repo.session.getSessionByIdWithUser(UUID.randomUUID()).expectNull()
    }
}
