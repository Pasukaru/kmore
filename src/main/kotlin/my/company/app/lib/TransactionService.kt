package my.company.app.lib

import com.zaxxer.hikari.pool.HikariPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.company.app.lib.koin.eager

class TransactionService {
    private val pool = eager<HikariPool>()

    suspend fun <T> transaction(block: suspend CoroutineScope.() -> T): T {
        val connection = withContext(Dispatchers.IO) { pool.connection }
        val tx = TransactionContext(connection)

        tx.beginTransaction()

        try {
            val result = withContext(tx, block)

            tx.commit()

            return result
        } catch (e: Throwable) {
            tx.rollback()
            throw e
        } finally {
            pool.evictConnection(connection)
        }
    }

    suspend fun <T> noTransaction(block: suspend CoroutineScope.() -> T): T {
        val connection = withContext(Dispatchers.IO) { pool.connection }
        val tx = TransactionContext(connection)
        try {
            return withContext(tx, block)
        } finally {
            pool.evictConnection(connection)
        }
    }
}
