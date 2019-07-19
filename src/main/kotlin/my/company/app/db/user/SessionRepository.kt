package my.company.app.db.user

import my.company.app.lib.repository.AbstractRepository
import my.company.jooq.Tables.SESSION
import my.company.jooq.tables.Session
import my.company.jooq.tables.records.SessionRecord
import java.util.UUID

open class SessionRepository : AbstractRepository<UUID, Session, SessionRecord>(SESSION)
