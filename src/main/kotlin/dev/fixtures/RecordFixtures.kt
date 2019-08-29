package dev.fixtures

import my.company.app.generated.jooq.tables.records.SessionRecord
import my.company.app.generated.jooq.tables.records.UserRecord
import my.company.app.lib.Faker
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

@Suppress("LongParameterList")
interface RecordFixtures {
    companion object Companion {
        const val MIN_CRYPTO_LOG_ROUNDS = 4
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
