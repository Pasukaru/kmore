package my.company.app.lib.repository

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import my.company.app.lib.tx.DbScheduler
import my.company.app.lib.tx.TransactionContext
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Query
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL
import java.sql.SQLDataException
import kotlin.reflect.full.isSubclassOf

abstract class AbstractRepository<ID, TABLE : Table<RECORD>, RECORD : Record>(
    table: TABLE
) : Repository {

    @Suppress("UNCHECKED_CAST")
    protected val table: TABLE = table.`as`(table.unqualifiedName) as TABLE

    @Suppress("UNCHECKED_CAST")
    protected open val primaryKey: Field<ID>
        get() = table.field(table.primaryKey.fields.first().name) as Field<ID>

    protected open val dialect: SQLDialect get() = SQLDialect.POSTGRES_9_5
    protected open val dsl: DSLContext get() = DSL.using(dialect)

    open suspend fun findById(id: ID): RECORD? =
        db { connectionDsl().select().from(table).where(primaryKey.eq(id)).fetchOne()?.into(table) }

    open suspend fun findAll(): List<RECORD> = db { connectionDsl().select().from(table).fetch().into(table) }

    open suspend fun insert(record: RECORD, dialect: SQLDialect = this.dialect): RECORD {
        val q = dsl.insertInto(table).set(record)
        val rows = execute(q)
        if (rows != 1) throw SQLDataException("Failed to insert row")
        return record
    }

    open suspend fun update(record: RECORD, dialect: SQLDialect = this.dialect): RECORD = throw NotImplementedError()

    open suspend fun deleteById(id: ID): Unit = db {
        val result = connectionDsl().deleteFrom(table).where(primaryKey.eq(id)).execute()
        if (result != 1) throw SQLDataException("Failed to delete by id: $id")
    }

    // I also tried to inline this one (and cross inline `block`) but it didn't noticeably affect performance.
    protected suspend fun <T> dbAsync(block: suspend (TransactionContext) -> T): Deferred<T> = coroutineScope {
        // Offload the blocking JDBC IO to the DB IO Connection pool, so that the other threads can continue working on other things
        val tx = coroutineContext[TransactionContext] ?: throw IllegalStateException("No active database session")
        async(DbScheduler) { tx.execute { block(tx) } }
    }

    protected suspend inline fun <T> db(crossinline block: suspend TransactionContext.() -> T): T =
        coroutineScope { dbAsync { tx -> block(tx) }.await() }


    protected suspend fun fetchOne(op: () -> Query): RECORD? = db {
        val query = op()
        val dialect = query.configuration().dialect()
        val sql = query.sql
        val params = query.bindValues
        val dsl = connectionDsl(dialect = dialect)
        @Suppress("SpreadOperator")
        val result = dsl.fetchOne(sql, *params.toTypedArray())
        when {
            result == null -> null
            result::class.isSubclassOf(table.recordType.kotlin) -> {
                @Suppress("UNCHECKED_CAST")
                result as RECORD
            }
            else -> result.into(table)
        }
    }


    protected suspend fun execute(query: Query): Int = db {
        val dialect = query.configuration().dialect()
        val sql = query.sql
        val params = query.bindValues
        val dsl = connectionDsl(dialect = dialect)
        @Suppress("SpreadOperator")
        dsl.execute(sql, *params.toTypedArray())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun TransactionContext.connectionDsl(dialect: SQLDialect = this@AbstractRepository.dialect): DSLContext =
        DSL.using(this.connection, dialect)
}
