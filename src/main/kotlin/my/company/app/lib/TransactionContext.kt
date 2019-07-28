package my.company.app.lib

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.impl.DSL
import java.sql.Connection
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class TransactionContext(
    private val connection: Connection
) : AbstractCoroutineContextElement(TransactionContext) {

    companion object Key : CoroutineContext.Key<TransactionContext> {
        val logger = logger<TransactionContext>()
    }

    val id = UUID.randomUUID()!!

    var committed: Boolean = false
        private set

    var rolledBack: Boolean = false
        private set

    private val beforeCommitHooks = mutableListOf<() -> Unit>()
    private val afterCommitHooks = mutableListOf<() -> Unit>()
    private val afterRollbackHooks = mutableListOf<() -> Unit>()
    private val afterCompletionHooks = mutableListOf<() -> Unit>()

    suspend fun beginTransaction() {
        execute {
            logger.debug("${this@TransactionContext}: BEGIN TRANSACTION")
            DSL.using(connection).execute("BEGIN TRANSACTION")
        }
    }

    suspend fun commit() {
        beforeCommitHooks.forEach { it.invoke() }
        execute {
            logger.debug("${this@TransactionContext}: COMMIT")
            DSL.using(connection).execute("COMMIT")
            committed = true
        }
        afterCommitHooks.forEach { it.invoke() }
        afterCompletionHooks.forEach { it.invoke() }
    }

    suspend fun rollback() {
        execute {
            logger.debug("${this@TransactionContext}: ROLLBACK")
            DSL.using(connection).execute("ROLLBACK")
            rolledBack = true
        }
        afterRollbackHooks.forEach { it.invoke() }
        afterCompletionHooks.forEach { it.invoke() }
    }

    private fun expectActive() {
        if (committed || rolledBack) error("Transaction is already completed")
    }

    suspend fun <T> execute(op: Connection.() -> T): T = withContext(Dispatchers.IO) {
        logger.trace("${this@TransactionContext}: Waiting for lock")
        try {
            synchronized(connection) {
                logger.trace("${this@TransactionContext}: Acquired lock")
                expectActive()
                op(connection)
            }
        } finally {
            logger.trace("${this@TransactionContext}: Released lock")
        }
    }

    fun beforeCommit(block: () -> Unit) = beforeCommitHooks.add(block)
    fun afterCommit(block: () -> Unit) = afterCommitHooks.add(block)
    fun afterRollback(block: () -> Unit) = afterRollbackHooks.add(block)
    fun afterCompletion(block: () -> Unit) = afterCompletionHooks.add(block)

    override fun toString(): String = "TransactionContext[$id]"
}
