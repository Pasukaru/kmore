import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import server.user.UserRepository
import tx.DbScheduler
import tx.transaction2Async
import tx.transactionAsync
import java.time.Instant
import java.util.*

suspend fun main() = coroutineScope {
    val repository = UserRepository()
    val userId = UUID.randomUUID()

    transactionAsync {

        val num = 1_000
        val itemDelay = 1L
        println()

        log("warm up")
        (0..num)
            .map { repository.noOp(itemDelay) }
            .forEach { it.await() }

        (0..num).map { async(DbScheduler) { } }.forEach { it.await() }

        log("==== A LOT OF CONCURRENT QUERIES (within the same transaction they will be scheduled in the db io threads, but still need to sync due to jdbc")

        val start = Instant.now().toEpochMilli()
        (0..num)
            .map { repository.noOp(itemDelay) }
            .forEach { it.await() }

        val afterQueries = Instant.now().toEpochMilli()

        (0..num).map { async(DbScheduler) { } }.forEach { it.await() }

        val afterActualNoOp = Instant.now().toEpochMilli()

        log("custom logic overhead for $num concurrent items: ${afterQueries - start - (afterActualNoOp - afterQueries) - itemDelay*num}ms")
    }.await()


    println()
    println()
    log("==== A LOT OF CONCURRENT TRANSACTIONS (This sohuld take 2s (1s to aq connection + 1s query)")
    (0..50)
        .map { transaction2Async { repository.noOp(1000) } }
        .also { log("==== SPAWNED") }
        .forEach { it.join() }
        .also { log("==== AWAITED") }


    transactionAsync {
        println()
        println()
        log("==== NESTED TRANSACTIONS")

        log("Before new tx")
        repository.findById(userId).await()

        val result = transactionAsync {
            log("Within new tx")
            repository.findById(userId).await()
        }.await()
        log("INNER TX RESULT: $result")

        log("After new tx")
        repository.findById(userId).await()
    }.await()

    try {
        transactionAsync {
            println()
            println()
            log("==== NESTED TRANSACTIONS WITH ROLLBACK")

            log("Before new tx")
            repository.findById(userId).await()

            val result = transactionAsync {
                log("Within new tx")
                repository.findById(userId).await()
            }.await()
            log("INNER TX RESULT: $result")

            throw RuntimeException("TRANSACTION KILLER")
        }.await()
    } catch (e: Throwable) {
        log("CAUGHT EXPECTED ERROR: ${e::class.java.simpleName} ${e.message}")
    }

    println()
    println()
    log("==== WITHOUT TRANSACTION")
    try {
        repository.findById(userId).await()
    } catch (e: Throwable) {
        log("CAUGHT EXPECTED ERROR: ${e::class.java.simpleName} ${e.message}")
    }

    joinAll()
}