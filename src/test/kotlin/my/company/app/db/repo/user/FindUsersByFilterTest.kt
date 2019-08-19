package my.company.app.db.repo.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import my.company.app.business_logic.user.GetUsersFilter
import my.company.app.db.repo.AbstractRepositoryTest
import my.company.app.test.expectOne
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant

class FindUsersByFilterTest : AbstractRepositoryTest() {
    companion object {
        private val EMPTY_FILTER = GetUsersFilter(
            email = null,
            name = null,
            createdAtBefore = null
        )
    }

    @Test
    fun canFindByEmail() = queryTest {
        val userA = fixtures.user()
        val userB = fixtures.user()

        val filter = EMPTY_FILTER.copy(
            email = " " + userA.email.toUpperCase() + " "
        )

        val foundUser = repo.user.findByFilter(filter).expectOne()
        assertThat(foundUser.changed()).isFalse()
        assertThat(foundUser).isEqualTo(userA)
        assertThat(foundUser).isNotEqualTo(userB)
    }

    @Test
    fun canFindByName() = queryTest {
        val userA = fixtures.user()
        val userB = fixtures.user()

        val filter = EMPTY_FILTER.copy(
            name = " " + userA.firstName.takeLast(2).toUpperCase() + " " + userA.lastName.take(2).toUpperCase() + " "
        )

        val foundUser = repo.user.findByFilter(filter).expectOne()
        assertThat(foundUser.changed()).isFalse()
        assertThat(foundUser).isEqualTo(userA)
        assertThat(foundUser).isNotEqualTo(userB)
    }

    @Test
    fun canFindByCreatedAtBefore() = queryTest {
        val time = Instant.now()

        Mockito.doAnswer { time }.`when`(mockedTimeService).now()
        val userA = fixtures.user()

        Mockito.doAnswer { time.plusSeconds(1) }.`when`(mockedTimeService).now()
        val userB = fixtures.user()

        val filter = EMPTY_FILTER.copy(
            createdAtBefore = userB.createdAt
        )

        val foundUser = repo.user.findByFilter(filter).expectOne()
        assertThat(foundUser.changed()).isFalse()
        assertThat(foundUser).isEqualTo(userA)
        assertThat(foundUser).isNotEqualTo(userB)
    }
}
