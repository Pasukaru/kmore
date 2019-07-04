
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.coroutineContext


class Repository {
    private var count = 1

    suspend fun findById(): Deferred<String> = dbIO {
        log("CTX coroutineContext[TransactionContext]     : ${coroutineContext[TransactionContext]?.connection}")
        log("CTX TransactionContext.getCurrentConnection(): ${TransactionContext.getCurrentConnection()}")
        return@dbIO "findByIdResult(${count++})"
    }

    private suspend inline fun <T> dbIO(crossinline block: suspend () -> T): Deferred<T> = coroutineScope {
        // Ensure DB IO is offloaded into the IO Connection pool
        // Unfortunately, we still need to synchronize because of JDBC
        val tx = coroutineContext[TransactionContext] ?: throw IllegalStateException("No active database session")
        async(coroutineContext + IOScheduler) {
            tx.semaphore.acquire()
            try {
                block()
            } finally {
                tx.semaphore.release()
            }
        }
    }
}