package my.company.app.lib.jooq

import org.jooq.Query

fun Query.withConnection() = QueryWithConnection(this)

