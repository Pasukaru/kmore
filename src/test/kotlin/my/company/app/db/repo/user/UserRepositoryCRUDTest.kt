package my.company.app.db.repo.user

import assertk.assertThat
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import my.company.app.db.repo.CRUDTest
import my.company.app.lib.Faker
import my.company.app.test.expectFalse
import my.company.app.test.expectNotNull
import my.company.app.test.expectNull
import my.company.app.test.expectTrue
import my.company.app.test.fixtures.InMemoryFixtures
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UserRepositoryCRUDTest : CRUDTest() {
    @Test
    override fun canInsert() = queryTest {
        val expected = InMemoryFixtures.user(
            createdAt = mockedTimeService.now()
        )

        val created = repo.user.insert(expected.copy().also {
            it.id = expected.id
            it.createdAt = Instant.now().minusSeconds(1000)
        })
        assertThat(created).isEqualTo(expected)
        assertThat(created.changed()).isFalse()
    }

    @Test
    override fun canFindById() = queryTest {
        val existing = fixtures.user()

        val found = repo.user.findById(existing.id).expectNotNull()
        assertThat(found).isEqualTo(existing)
        assertThat(found.changed()).isFalse()

        assertThat(repo.user.findById(UUID.randomUUID())).isNull()
    }

    @Test
    override fun canFindAll() = queryTest {
        val existing = (1..3).map { fixtures.user() }.associateBy { it.id }

        val found = repo.user.findAll()
        assertThat(found).hasSameSizeAs(existing.keys)
        found.forEach {
            assertThat(it.changed()).isFalse()
            assertThat(it).isEqualTo(existing[it.id])
        }
    }

    @Test
    override fun canUpdate() = queryTest {
        val existing = fixtures.user()
        val expected = InMemoryFixtures.user().also {
            it.id = existing.id
            it.updatedAt = mockedTimeService.now()
            it.changed(false)
        }

        val updated = repo.user.update(expected.copy().also { it.id = existing.id })
        assertThat(updated).isEqualTo(expected)
        assertThat(repo.user.findById(existing.id)).isEqualTo(expected)
    }

    @Test
    override fun canDeleteById() = queryTest {
        val existing = fixtures.user()
        repo.user.findById(existing.id).expectNotNull()
        repo.user.deleteById(existing.id)
        repo.user.findById(existing.id).expectNull()
    }

    @Test
    fun canFindByEmailIgnoringCase() = queryTest {
        val existing = fixtures.user(email = Faker.internet().emailAddress().toUpperCase())
        repo.user.findByEmailIgnoringCase(existing.email.toUpperCase())
            .expectNotNull("Should find ignoring case")
        repo.user.findByEmailIgnoringCase("   " + existing.email.toUpperCase() + "   ")
            .expectNotNull("Should find with trimmed input")
        repo.user.findByEmailIgnoringCase("1" + existing.email)
            .expectNull("Should not find with wrong email")
    }

    @Test
    fun existsFindByEmailIgnoringCase() = queryTest {
        val existing = fixtures.user(email = Faker.internet().emailAddress().toUpperCase())
        repo.user.existsByEmailIgnoringCase(existing.email.toUpperCase())
            .expectTrue("Should find ignoring case")
        repo.user.existsByEmailIgnoringCase("   " + existing.email.toUpperCase() + "   ")
            .expectTrue("Should find with trimmed input")
        repo.user.existsByEmailIgnoringCase("1" + existing.email)
            .expectFalse("Should not find with wrong email")
    }
}
