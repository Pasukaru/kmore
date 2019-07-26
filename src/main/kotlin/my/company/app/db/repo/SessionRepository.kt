package my.company.app.db.repo

import my.company.app.lib.repository.AbstractRepository
import my.company.jooq.Tables.SESSION
import my.company.jooq.tables.Session
import my.company.jooq.tables.records.SessionRecord
import java.util.UUID

open class SessionRepository : AbstractRepository<UUID, Session, SessionRecord>(SESSION) {
    override fun beforeInsert(record: SessionRecord) {
        record.createdAt = timeService.now()
    }

    override fun beforeUpdate(record: SessionRecord) {
        record.updatedAt = timeService.now()
    }
}
