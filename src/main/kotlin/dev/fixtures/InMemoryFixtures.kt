package dev.fixtures

import dev.fixtures.RecordFixtures.Companion.MIN_CRYPTO_LOG_ROUNDS
import dev.fixtures.RecordFixtures.Companion.SECURE_RANDOM
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import org.jooq.Record
import org.springframework.security.crypto.bcrypt.BCrypt
import java.time.Instant
import java.util.UUID

object InMemoryFixtures : RecordFixtures {

    private fun <T : Record> afterInit(record: T): T {
        // Mark record as unchanged, imitating it has been saved to database
        record.changed(false)
        return record
    }

    override suspend fun user(
        id: UUID,
        email: String,
        firstName: String,
        lastName: String,
        passwordClean: String,
        createdAt: Instant,
        updatedAt: Instant?
    ): UserRecord {
        return afterInit(UserRecord().also {
            it.id = id
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.password = BCrypt.hashpw(passwordClean, BCrypt.gensalt(MIN_CRYPTO_LOG_ROUNDS, SECURE_RANDOM))
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        })
    }

    override suspend fun session(
        id: UUID,
        userId: UUID,
        createdAt: Instant,
        updatedAt: Instant?
    ): SessionRecord {
        return afterInit(SessionRecord().also {
            it.id = id
            it.userId = userId
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        })
    }
}
