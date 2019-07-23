package my.company.app.test

import my.company.app.lib.Faker
import my.company.app.test.AbstractFixtures.Companion.SECURE_RANDOM
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import org.jooq.Record
import org.springframework.security.crypto.bcrypt.BCrypt
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

open class AbstractFixtures {

    private object Companion {
        val SECURE_RANDOM = SecureRandom()
    }

    protected open fun afterInit(record: Record) {
        // Mark record as unchanged, imitating it has been saved to database
        record.changed(false)
    }

    fun user(
        id: UUID = UUID.randomUUID(),
        email: String = Faker.internet().emailAddress(),
        firstName: String = Faker.name().firstName(),
        lastName: String = Faker.name().lastName(),
        passwordClean: String = Faker.internet().password(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant? = null
    ): UserRecord {
        return UserRecord().also {
            it.id = id
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.password = BCrypt.hashpw(passwordClean, BCrypt.gensalt(4, SECURE_RANDOM))
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        }.also { afterInit(it) }
    }

    fun session(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant? = null
    ): SessionRecord {
        return SessionRecord().also {
            it.id = id
            it.userId = userId
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        }
    }
}

object Fixtures : AbstractFixtures()
