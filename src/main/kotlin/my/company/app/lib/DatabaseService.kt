package my.company.app.lib

import com.zaxxer.hikari.pool.HikariPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.company.app.db.IsolationLevel
import my.company.app.lib.koin.lazy
import org.jooq.SQLDialect
import java.sql.Connection
import kotlin.coroutines.coroutineContext

class DatabaseService {
    private val pool by lazy<HikariPool>()
    private val sqlDialect: SQLDialect = SQLDialect.POSTGRES_9_5

    /**
     * Acquires a connection from the connection.
     * The blocking action of waiting for a valid connection is dispatched via [Dispatchers.IO][kotlinx.coroutines.Dispatchers.IO]
     * The connection will be evicted once [block] returns or throws an error.
     */
    suspend fun <T> withConnection(block: suspend (connection: Connection) -> T): T {
        val connection = withContext(Dispatchers.IO) { pool.connection }
        return try {
            block(connection)
        } finally {
            pool.evictConnection(connection)
        }
    }

    /**
     * Creates a coroutine context with an active database connection and starts a new transaction.
     * Then calls [block] within that context.
     *
     * If [inherit] is true, the surrounding transaction will be used instead.
     * This will fail in case the surrounding transaction has a different isolation level or read only property.
     * If no surrounding transaction exists, a new one will be created.
     * When the transaction is inherited from the surrounding scope, it will not be committed when this function returns.
     *
     * The transaction will be committed if [block] returns normally.<br/>
     * The transaction will be rolled back if [block] throws an exception.
     *
     * @see [withConnection]
     */
    suspend fun <T> transaction(
        isolationLevel: IsolationLevel = IsolationLevel.READ_COMMITTED,
        readOnly: Boolean = false,
        inherit: Boolean = true,
        block: suspend CoroutineScope.() -> T
    ): T {
        val existingTx = coroutineContext[TransactionContext]

        return if (existingTx != null && inherit) {
            if (isolationLevel != existingTx.isolationLevel) error("Cannot inherit transaction with different isolation level: ${existingTx.isolationLevel} != $isolationLevel")
            if (readOnly != existingTx.readOnly) error("Cannot inherit transaction with different readonly property: ${existingTx.readOnly} != $readOnly")
            withContext(coroutineContext, block)
        } else {
            withConnection { connection ->
                val tx = TransactionContext(connection, sqlDialect)
                tx.transaction(isolationLevel, readOnly) {
                    block()
                }
            }
        }
    }
}
