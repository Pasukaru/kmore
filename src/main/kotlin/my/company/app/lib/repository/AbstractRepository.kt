package my.company.app.lib.repository

import my.company.app.lib.jooq.withConnection
import my.company.app.lib.logger
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL
import java.sql.SQLDataException

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

    open suspend fun findById(id: ID): RECORD? = dsl.select().from(table).where(primaryKey.eq(id)).withConnection().fetchOne()?.into(table)
    open suspend fun findAll(): List<RECORD> = dsl.select().from(table).withConnection().fetch().into(table)

    open suspend fun insert(record: RECORD): RECORD {
        val rows = dsl.insertInto(table).set(record).withConnection().execute()
        if (rows != 1) throw SQLDataException("Failed to insert row")
        record.changed(false)
        return record
    }

    @Suppress("UNCHECKED_CAST")
    protected open val RECORD.id : ID get() = this["id"] as ID

    open suspend fun update(record: RECORD): RECORD {
        val rows = dsl.update(table).set(record).where(primaryKey.eq(record.id)).withConnection().execute()
        if (rows != 1) throw SQLDataException("Failed to update row")
        record.changed(false)
        return record
    }

    open suspend fun deleteById(id: ID) {
        val result = dsl.deleteFrom(table).where(primaryKey.eq(id)).withConnection().execute()
        if (result != 1) throw SQLDataException("Failed to delete by id: $id")
    }

}
