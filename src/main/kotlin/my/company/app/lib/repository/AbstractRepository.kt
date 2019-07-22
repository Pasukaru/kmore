package my.company.app.lib.repository

import my.company.app.lib.TransactionContext
import my.company.app.lib.logger
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Query
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.SQLDataException
import kotlin.coroutines.coroutineContext
import kotlin.reflect.full.isSubclassOf

abstract class AbstractRepository<ID, TABLE : Table<RECORD>, RECORD : Record>(
    table: TABLE
) : Repository {

    protected val logger = this::class.logger()

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

    open suspend fun insert(record: RECORD): RECORD {
        val q = dsl.insertInto(table).set(record)
        val rows = execute(q)
        if (rows != 1) throw SQLDataException("Failed to insert row")
        return record
    }

    @Suppress("UNCHECKED_CAST")
    protected open val RECORD.id : ID get() = this["id"] as ID

    open suspend fun update(record: RECORD): RECORD {
        val q = dsl.update(table).set(record).where(primaryKey.eq(record.id))
        val rows = execute(q)
        if (rows != 1) throw SQLDataException("Failed to update row")
        return record
    }

    open suspend fun deleteById(id: ID): Unit = db {
        val result = connectionDsl().deleteFrom(table).where(primaryKey.eq(id)).execute()
        if (result != 1) throw SQLDataException("Failed to delete by id: $id")
    }

    protected suspend fun <T> db(block: suspend Connection.() -> T): T {
        val start = System.currentTimeMillis()

        val tx = coroutineContext[TransactionContext] ?: throw IllegalStateException("No active database session")
        val result = tx.execute(block)

        val time = System.currentTimeMillis() - start
        logger.trace("Query completed in ${time}ms")

        return result
    }

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
    protected open fun Connection.connectionDsl(dialect: SQLDialect = this@AbstractRepository.dialect): DSLContext =
        DSL.using(this, dialect)
}
