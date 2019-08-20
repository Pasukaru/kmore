package my.company.app.lib

import com.zaxxer.hikari.pool.HikariPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.company.app.lib.koin.eager
import java.sql.Connection

class DatabaseService {
    private val pool = eager<HikariPool>()

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
     * The transaction will be committed if [block] returns normally.<br/>
     * The transaction will be rolled back if [block] throws an exception.
     *
     * @see [withConnection]
     */
    suspend fun <T> transaction(block: suspend CoroutineScope.() -> T): T = withConnection { connection ->
        val tx = TransactionContext(connection)

        tx.beginTransaction()

        try {
            val result = withContext(tx, block)

            tx.commit()

            return@withConnection result
        } catch (e: Throwable) {
            tx.rollback()
            throw e
        }
    }

    /**
     * Creates a coroutine context with an active database connection that does not use any transaction management.
     * Then calls [block] within that context.
     *
     * @see [withConnection]
     */
    suspend fun <T> noTransaction(block: suspend CoroutineScope.() -> T): T = withConnection { connection ->
        val tx = TransactionContext(connection)
        return@withConnection withContext(tx, block)
    }
}
