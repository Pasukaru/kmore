package my.company.app.db.query

import my.company.app.lib.jooq.joinOn
import my.company.jooq.Tables.SESSION
import my.company.jooq.Tables.USER
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord
import org.jooq.DSLContext
import org.jooq.JoinType
import org.jooq.ResultQuery
import java.util.UUID

class FindSessionByIdWithUserQuery(
    dsl: DSLContext,
    private val sessionId: UUID
) : CustomJooqQuery<Pair<SessionRecord, UserRecord>?>(dsl) {
    private val tSession = SESSION.`as`("s")
    private val tUser = USER.`as`("u")

    override fun DSLContext.buildInternal(): ResultQuery<*> =
        select(tSession.asterisk(), tUser.asterisk())
            .from(tSession)
            .joinOn(tSession, tUser, JoinType.JOIN)
            .where(tSession.ID.eq(sessionId))

    override fun ResultQuery<*>.executeInternal(): Pair<SessionRecord, UserRecord>? =
        fetchOne()?.let { r ->
            val session = r.into(tSession)
            val user = r.into(tUser)
            session to user
        }
}
