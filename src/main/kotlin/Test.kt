
import kotlinx.coroutines.*
import java.sql.Connection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

data class TransactionContext(
    val connection: Connection
) : AbstractCoroutineContextElement(TransactionContext) {
    companion object Key : CoroutineContext.Key<TransactionContext> {
        suspend fun getCurrentConnection() : Connection? = coroutineContext[TransactionContext]?.connection
    }
}

suspend fun main() = coroutineScope {
    transaction {
        println("\n\n==== TRANSACTION")
        println(findById())
    }.join()

    transaction {
        launch {
            println("\n\n==== INNER LAUNCH")
            println(findById())
        }
    }.join()

    transaction {
        println("\n\n==== NESTED SUSPENSIONS")
        launch { nestStuff() }
    }.join()

    transaction {
        println("\n\n==== NESTED TRANSACTIONS")

        println("Before new tx")
        findById()

        transaction {
            println("Within new tx")
            findById()
        }.join()

        println("After new tx")
        findById()
    }.join()
}

fun CoroutineScope.transaction(
    context: CoroutineContext = Dispatchers.Unconfined + TransactionContext(FakeConnection.createConnection()),
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) : Job = this.launch(context, start, block)

suspend fun nestStuff() {
    println("nested suspended call #1")
    findById()
    println("nested suspended call #2")
    findById()
}

suspend fun findById() : String {
    println("CTX coroutineContext[TransactionContext]     : ${coroutineContext[TransactionContext]?.connection}")
    println("CTX TransactionContext.getCurrentConnection(): ${TransactionContext.getCurrentConnection()}")
    return "findByIdResult"
}