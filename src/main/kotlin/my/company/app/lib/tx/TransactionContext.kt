package my.company.app.lib.tx

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import my.company.app.FakeConnection
import my.company.app.log
import java.sql.Connection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class TransactionContext(
    private val connection: Connection
) : AbstractCoroutineContextElement(TransactionContext) {

    companion object Key : CoroutineContext.Key<TransactionContext>

    val mutex: Mutex = Mutex()
    var committed: Boolean = false
        private set

    var rolledBack: Boolean = false
        private set

    suspend fun commit() {
        execute {
            log("COMMIT")
            // tx.connection.prepareStatement("COMMIT").execute()
            committed = true
        }
    }

    suspend fun rollback() {
        execute {
            log("ROLLBACK")
            // tx.connection.prepareStatement("ROLLBACK").execute()
            rolledBack = true
        }
    }

    fun expectActive() {
        if (committed || rolledBack) throw IllegalStateException("Transaction is already completed")
    }

    suspend fun <T> execute(op: suspend Connection.() -> T): T {
        mutex.lock()
        log("CTX coroutineContext[my.company.TransactionContext]: ${coroutineContext[TransactionContext]}")
        return try {
            expectActive()
            op(connection)
        } finally {
            mutex.unlock()
        }
    }
}

suspend inline fun <T> transactionAsync(crossinline block: suspend () -> T): Deferred<T> = coroutineScope {
    val tx = TransactionContext(FakeConnection.createConnection())
    async(coroutineContext + tx) {
        try {
            val result = block()
            log("<TX_COMMIT> $tx")
            return@async result
        } catch (e: Throwable) {
            log("<TX_ROLLBACK> $tx")
            e.printStackTrace()
            throw e
        }
    }
}

fun <T> CoroutineScope.transaction2Async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    // Use DB IO thread to await connection
    return async(DbScheduler) {
        val tx = TransactionContext(FakeConnection.createConnection())
        // Switch back to my.company.main threads for the transaction
        try {
            val result = async(Dispatchers.Default + tx, CoroutineStart.DEFAULT, block).await()
            tx.commit()
            result
        } catch (e: Throwable) {
            tx.rollback()
            throw e
        }
    }
}
