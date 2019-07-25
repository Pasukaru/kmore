package my.company.app.lib

import com.zaxxer.hikari.pool.HikariPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import my.company.app.lib.koin.eager

class TransactionService {
    suspend fun <T> transaction(block: suspend CoroutineScope.() -> T): T {
        val pool = eager<HikariPool>()
        val tx = TransactionContext(pool.connection)

        tx.beginTransaction()

        try {
            val result = withContext(tx, block)

            tx.commit()

            return result
        } catch (e: Throwable) {
            tx.rollback()
            throw e
        } finally {
            pool.evictConnection(tx.connection)
        }
    }

    suspend fun <T> noTransaction(block: suspend CoroutineScope.() -> T): T {
        val pool = eager<HikariPool>()
        val tx = TransactionContext(pool.connection)
        try {
            return withContext(tx, block)
        } catch (e: Throwable) {
            throw e
        } finally {
            pool.evictConnection(tx.connection)
        }
    }
}
