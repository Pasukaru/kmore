package my.company.app.db

import my.company.app.lib.IdGenerator
import my.company.app.lib.TimeService
import my.company.app.lib.lazy
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import java.util.UUID

class ModelGenerator {
    private val idGenerator: IdGenerator by lazy()
    private val timeService: TimeService by lazy()

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
            it.createdAt = timeService.now()
        }
    }

    fun session(
        userId: UUID
    ): SessionRecord {
        return SessionRecord().also {
            it.id = idGenerator.uuid()
            it.userId = userId
            it.createdAt = timeService.now()
        }
    }
}