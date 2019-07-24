package my.company.app.test.fixtures

import my.company.app.lib.lazy
import my.company.app.lib.repository.Repositories
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import org.springframework.security.crypto.bcrypt.BCrypt
import java.time.Instant
import java.util.UUID

class DbFixtures : RecordFixtures {

    private val repo by lazy<Repositories>()

    override suspend fun user(
        id: UUID,
        email: String,
        firstName: String,
        lastName: String,
        passwordClean: String,
        createdAt: Instant,
        updatedAt: Instant?
    ): UserRecord {
        return repo.user.insert(UserRecord().also {
            it.id = id
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.password = BCrypt.hashpw(passwordClean, BCrypt.gensalt(4, RecordFixtures.SECURE_RANDOM))
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
        return repo.session.insert(SessionRecord().also {
            it.id = id
            it.userId = userId
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        })
    }
}
