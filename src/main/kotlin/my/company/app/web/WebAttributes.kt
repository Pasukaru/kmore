package my.company.app.web

import io.ktor.util.AttributeKey
import my.company.jooq.tables.records.SessionRecord
import my.company.jooq.tables.records.UserRecord

val SessionKey = AttributeKey<SessionRecord>("session")
val AuthenticatedUser = AttributeKey<UserRecord>("user")
