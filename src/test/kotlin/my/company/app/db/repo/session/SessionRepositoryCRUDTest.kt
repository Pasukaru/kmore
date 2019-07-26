package my.company.app.db.repo.session

import assertk.assertThat
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import my.company.app.db.repo.CRUDTest
import my.company.app.test.expectNotNull
import my.company.app.test.expectNull
import my.company.app.test.fixtures.InMemoryFixtures
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SessionRepositoryCRUDTest : CRUDTest() {
    @Test
    override fun canInsert() = queryTest {
        val user = fixtures.user()
        val expected = InMemoryFixtures.session(userId = user.id, createdAt = mockedTimeService.now())

        val created = repo.session.insert(expected.copy().also {
            it.id = expected.id
            it.createdAt = Instant.now().minusSeconds(1000)
        })
        assertThat(created).isEqualTo(expected)
        assertThat(created.changed()).isFalse()
    }

    @Test
    override fun canFindById() = queryTest {
        val user = fixtures.user()
        val existing = fixtures.session(userId = user.id, createdAt = mockedTimeService.now())

        val found = repo.session.findById(existing.id).expectNotNull()
        assertThat(found).isEqualTo(existing)
        assertThat(found.changed()).isFalse()

        assertThat(repo.session.findById(UUID.randomUUID())).isNull()
    }

    @Test
    override fun canFindAll() = queryTest {
        val users = (1..3).map { fixtures.user() }
        val existing = (1..3).map { fixtures.session(userId = users.random().id) }.associateBy { it.id }

        val found = repo.session.findAll()
        assertThat(found).hasSameSizeAs(existing.keys)
        found.forEach {
            assertThat(it.changed()).isFalse()
            assertThat(it).isEqualTo(existing[it.id])
        }
    }

    @Test
    override fun canUpdate() = queryTest {
        val user = fixtures.user()
        val user2 = fixtures.user()
        val existing = fixtures.session(userId = user.id)
        val expected = InMemoryFixtures.session(
            userId = user2.id
        ).also {
            it.id = existing.id
            it.updatedAt = mockedTimeService.now()
            it.changed(false)
        }

        val updated = repo.session.update(expected.copy().also { it.id = existing.id })
        assertThat(updated).isEqualTo(expected)
        assertThat(repo.session.findById(existing.id)).isEqualTo(expected)
    }

    @Test
    override fun canDeleteById() = queryTest {
        val user = fixtures.user()
        val existing = fixtures.session(userId = user.id)
        repo.session.findById(existing.id).expectNotNull()
        repo.session.deleteById(existing.id)
        repo.session.findById(existing.id).expectNull()
    }
}
