package my.company.app.db

import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import java.time.Instant
import java.util.UUID

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

fun newSession(
    userId: UUID
): SessionRecord {
    return SessionRecord().also {
        it.id = UUID.randomUUID()
        it.userId = userId
        it.createdAt = Instant.now()
    }
}
