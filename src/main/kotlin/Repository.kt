
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.coroutineContext

open class Repository {
    private var count = 1

    suspend fun findById(): Deferred<String> = dbAsync {
        log("CTX coroutineContext[TransactionContext]     : ${coroutineContext[TransactionContext]?.connection}")
        return@dbAsync "findByIdResult(${count++})"
    }

    suspend fun noOp(delayInMs: Long = 0): Deferred<Unit> = dbAsync {
        @Suppress("BlockingMethodInNonBlockingContext")
        if(delayInMs > 0) Thread.sleep(delayInMs) // JDBC would block the whole thread
    }

    // I also tried to inline this one (and cross inline `block`) but it didn't noticeably affect performance.
    @Suppress("MemberVisibilityCanBePrivate")
    protected suspend fun <T> dbAsync(block: suspend (TransactionContext) -> T): Deferred<T> = coroutineScope {
        // Offload the blocking JDBC IO to the DB IO Connection pool, so that the other threads can continue working on other things
        val tx = coroutineContext[TransactionContext] ?: throw IllegalStateException("No active database session")
        async(DbScheduler) {
            // Unfortunately, we still need to synchronize the connection usage because of JDBC
            tx.mutex.lock()
            try {
                // Just a test to see whether the mutex is working as I expect it. This exception is never thrown if it is.
                if (tx.currentlyExecuting) throw IllegalStateException("Connection is already being used! This would cause mayhem with a JDBC connection!")
                tx.currentlyExecuting = true
                try {
                    block(tx)
                } finally {
                    tx.currentlyExecuting = false
                }
            } finally {
                tx.mutex.unlock()
            }
        }
    }
}