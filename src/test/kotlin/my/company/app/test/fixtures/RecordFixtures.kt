package my.company.app.test.fixtures

import my.company.app.lib.Faker
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

interface RecordFixtures {
    companion object Companion {
        val SECURE_RANDOM = SecureRandom()
    }

    suspend fun user(
        id: UUID = UUID.randomUUID(),
        email: String = Faker.internet().emailAddress(),
        firstName: String = Faker.name().firstName(),
        lastName: String = Faker.name().lastName(),
        passwordClean: String = Faker.internet().password(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant? = null
    ): UserRecord

    suspend fun session(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant? = null
    ): SessionRecord
}
