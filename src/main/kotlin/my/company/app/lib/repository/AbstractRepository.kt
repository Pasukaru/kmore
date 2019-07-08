package my.company.app.lib.repository

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import my.company.app.lib.tx.DbScheduler
import my.company.app.lib.tx.TransactionContext

abstract class AbstractRepository : Repository {
    // I also tried to inline this one (and cross inline `block`) but it didn't noticeably affect performance.
    protected suspend fun <T> dbAsync(block: suspend (TransactionContext) -> T): Deferred<T> = coroutineScope {
        // Offload the blocking JDBC IO to the DB IO Connection pool, so that the other threads can continue working on other things
        val tx = coroutineContext[TransactionContext] ?: throw IllegalStateException("No active database session")
        async(DbScheduler) {
            tx.execute {
                block(tx)
            }
        }
    }
}