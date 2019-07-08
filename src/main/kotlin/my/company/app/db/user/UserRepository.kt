package my.company.app.db.user

import kotlinx.coroutines.Deferred
import my.company.app.lib.repository.AbstractRepository
import my.company.app.lib.tx.TransactionContext
import my.company.app.log
import java.util.*
import kotlin.coroutines.coroutineContext

open class UserRepository : AbstractRepository() {
    private var count = 1

    suspend fun findById(id: UUID): Deferred<DbUser> = dbAsync {
        log("FIND BY ID: " + coroutineContext[TransactionContext])
        return@dbAsync DbUser(id = id, name = "$count")
    }

    suspend fun noOp(delayInMs: Long = 0): Deferred<Unit> = dbAsync {
        @Suppress("BlockingMethodInNonBlockingContext")
        if (delayInMs > 0) Thread.sleep(delayInMs) // JDBC would block the whole thread
    }
}