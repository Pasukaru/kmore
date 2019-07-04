import kotlinx.coroutines.sync.Semaphore
import java.sql.Connection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

data class TransactionContext(
    val connection: Connection,
    val semaphore : Semaphore = Semaphore(1, 0)
) : AbstractCoroutineContextElement(TransactionContext) {
    companion object Key : CoroutineContext.Key<TransactionContext> {
        suspend fun getCurrentConnection(): Connection? = coroutineContext[TransactionContext]?.connection
    }
}