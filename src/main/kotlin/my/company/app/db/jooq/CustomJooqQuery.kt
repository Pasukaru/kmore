package my.company.app.db.jooq

import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.SQLDialect
import org.jooq.impl.DSL

abstract class CustomJooqQuery {
    abstract fun build(isCountQuery: Boolean): Query
    open val supportsCount: Boolean = false
    protected open val dsl: DSLContext = DSL.using(SQLDialect.POSTGRES_9_5)
}
