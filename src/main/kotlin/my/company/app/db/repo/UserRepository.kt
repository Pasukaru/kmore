package my.company.app.db.repo

import my.company.app.lib.jooq.withConnection
import my.company.app.lib.repository.AbstractRepository
import my.company.jooq.Tables.USER
import my.company.jooq.tables.User
import my.company.jooq.tables.records.UserRecord
import java.util.UUID

open class UserRepository : AbstractRepository<UUID, User, UserRecord>(USER) {
    suspend fun findByEmailIgnoringCase(email: String): UserRecord? =
        dsl.select().from(table).where(table.EMAIL.equalIgnoreCase(email.trim())).withConnection().fetchOne()?.into(table)

    suspend fun existsByEmailIgnoringCase(email: String): Boolean = findByEmailIgnoringCase(email) != null
}

