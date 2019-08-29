package my.company.app.db

import my.company.app.generated.jooq.tables.records.SessionRecord
import my.company.app.generated.jooq.tables.records.UserRecord
import my.company.app.lib.IdGenerator
import my.company.app.lib.koin.lazy
import java.util.UUID

class ModelGenerator {
    private val idGenerator: IdGenerator by lazy()

    fun user(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): UserRecord {
        return UserRecord().also {
            it.id = idGenerator.uuid()
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.password = password
        }
    }

    fun session(
        userId: UUID
    ): SessionRecord {
        return SessionRecord().also {
            it.id = idGenerator.uuid()
            it.userId = userId
        }
    }
}
