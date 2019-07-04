import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.sql.Connection
import java.time.Instant
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

data class TransactionContext(
    val connection: Connection
) : AbstractCoroutineContextElement(TransactionContext) {
    companion object Key : CoroutineContext.Key<TransactionContext> {
        suspend fun getCurrentConnection(): Connection? = coroutineContext[TransactionContext]?.connection
    }
}

fun log(msg: Any?) {
    println("${Instant.now()} - ${Thread.currentThread().name}: $msg")
}

val repository = Repository()

suspend fun main() = coroutineScope {
    transaction {
        log("==== NESTED TRANSACTIONS\n\n")

        log("Before new tx")
        repository.findById().await()

        val result = transaction {
            log("Within new tx")
            repository.findById().await()
        }.await()
        log("INNER TX RESULT: $result")

        log("After new tx")
        repository.findById().await()
    }.join()
    transaction {
        log("==== NESTED TRANSACTIONS WITH ROLLBACK\n\n")

        log("Before new tx")
        repository.findById().await()

        val result = transaction {
            log("Within new tx")
            repository.findById().await()
        }.await()
        log("INNER TX RESULT: $result")

        throw RuntimeException("DERP")
    }.join()
}


private suspend inline fun <T> transaction(crossinline block: suspend () -> T): Deferred<T> = coroutineScope {
    val tx = TransactionContext(FakeConnection.createConnection())
    async(coroutineContext + tx) {
        try {
            val result = block()
            log("<TX_COMMIT> $tx")
            return@async result
        } catch (e: Throwable) {
            log("<TX_ROLLBACK> $tx")
            throw e
        }
    }
}