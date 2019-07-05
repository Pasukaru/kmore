package server.user

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import log
import server.lib.repository.AbstractRepository
import server.user.dto.User
import tx.DbScheduler
import tx.TransactionContext
import java.util.*
import kotlin.coroutines.coroutineContext

open class UserRepository : AbstractRepository() {
    private var count = 1

    suspend fun findById(id: UUID): Deferred<User> = dbAsync {
        log("FIND BY ID: " + coroutineContext[TransactionContext])
        return@dbAsync User(id = id, name = "$count")
    }

    suspend fun noOp(delayInMs: Long = 0): Deferred<Unit> = dbAsync {
        @Suppress("BlockingMethodInNonBlockingContext")
        if (delayInMs > 0) Thread.sleep(delayInMs) // JDBC would block the whole thread
    }

    // I also tried to inline this one (and cross inline `block`) but it didn't noticeably affect performance.
    @Suppress("MemberVisibilityCanBePrivate")
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