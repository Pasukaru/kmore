import kotlinx.coroutines.sync.Mutex
import java.sql.Connection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class TransactionContext(
    val connection: Connection,
    val mutex: Mutex = Mutex(),
    var currentlyExecuting: Boolean = false
) : AbstractCoroutineContextElement(TransactionContext) {
    companion object Key : CoroutineContext.Key<TransactionContext>
}


