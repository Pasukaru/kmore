package my.company.app.db.repo

import my.company.app.business_logic.user.GetUsersFilter
import my.company.app.generated.jooq.Tables.USER
import my.company.app.generated.jooq.tables.records.UserRecord
import my.company.app.lib.repository.AbstractRepository
import org.jooq.impl.DSL.and
import org.jooq.impl.DSL.concat
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.inline
import java.util.UUID

open class UserRepository : AbstractRepository<UUID, my.company.app.generated.jooq.tables.User, UserRecord>(USER) {
    override fun beforeInsert(record: UserRecord) {
        record.createdAt = timeService.now()
    }

    override fun beforeUpdate(record: UserRecord) {
        record.updatedAt = timeService.now()
    }

    suspend fun findByFilter(filter: GetUsersFilter): List<UserRecord> {
        return query {
            select()
                .from(table)
                .where(and(listOfNotNull(
                    filter.email?.let { table.EMAIL.containsIgnoreCase(it.trim()) },
                    filter.name?.let { concat(table.FIRST_NAME, field(inline(" ")), table.LAST_NAME).containsIgnoreCase(it.trim()) },
                    filter.createdAtBefore?.let { table.CREATED_AT.lessThan(it) }
                )))
                .fetch().into(table)
        }
    }

    suspend fun findByEmailIgnoringCase(email: String): UserRecord? =
        query { select().from(table).where(table.EMAIL.equalIgnoreCase(email.trim())).fetchOne()?.into(table) }

    suspend fun existsByEmailIgnoringCase(email: String): Boolean = findByEmailIgnoringCase(email) != null
}

