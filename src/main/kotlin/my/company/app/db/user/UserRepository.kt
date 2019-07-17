package my.company.app.db.user

import my.company.app.lib.repository.AbstractRepository
import my.company.jooq.Tables.USER
import my.company.jooq.tables.User
import my.company.jooq.tables.records.UserRecord
import java.time.Instant
import java.util.*

open class UserRepository : AbstractRepository<UUID, User, UserRecord>(USER) {
    suspend fun findByEmailIgnoringCase(email: String): UserRecord? =
        fetchOne { dsl.select().from(table).where(table.EMAIL.equalIgnoreCase(email.trim())) }
}

fun newUser(
    email: String,
    firstName: String,
    lastName: String,
    password: String
): UserRecord {
    return UserRecord().also {
        it.id = UUID.randomUUID()
        it.email = email
        it.firstName = firstName
        it.lastName = lastName
        it.password = password
        it.createdAt = Instant.now()
    }
}
