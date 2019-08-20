package my.company.app.db.repo

import my.company.app.db.query.FindSessionByIdWithUserQuery
import my.company.app.lib.repository.AbstractRepository
import my.company.jooq.Tables.SESSION
import my.company.jooq.tables.Session
import my.company.jooq.tables.records.SessionRecord
import java.util.UUID
import org.jooq.JoinType.JOIN as INNER_JOIN

open class SessionRepository : AbstractRepository<UUID, Session, SessionRecord>(SESSION) {
    override fun beforeInsert(record: SessionRecord) {
        record.createdAt = timeService.now()
    }

    override fun beforeUpdate(record: SessionRecord) {
        record.updatedAt = timeService.now()
    }

    suspend fun getSessionByIdWithUser(sessionId: UUID) = query { FindSessionByIdWithUserQuery(this, sessionId).execute() }
}
