package my.company.app.lib.jooq

import my.company.app.lib.TransactionContext
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.Record
import org.jooq.Result
import org.jooq.impl.DSL
import kotlin.coroutines.coroutineContext

@Suppress("SpreadOperator") // Unfortunately jooq only provides API that takes varargs
class QueryWithConnection(query: Query) {
    private val sql: String = query.sql
    private val bindValues: Array<Any?> = query.bindValues.toTypedArray()

    suspend fun fetchOne(): Record? = withDSL { fetchOne(sql, *bindValues) }
    suspend fun fetch(): Result<Record> = withDSL { fetch(sql, *bindValues) }
    suspend fun execute() = withDSL { execute(sql, *bindValues) }
}

private suspend inline fun <T> withDSL(crossinline op: DSLContext.() -> T): T {
    val tx = coroutineContext[TransactionContext] ?: error("No active database session")
    return tx.execute { op(DSL.using(this)) }
}
