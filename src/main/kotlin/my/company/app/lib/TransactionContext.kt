package my.company.app.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import my.company.app.db.IsolationLevel
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class TransactionContext(
    private val connection: Connection,
    private val sqlDialect: SQLDialect = SQLDialect.POSTGRES_9_5,
    val dsl: DSLContext = DSL.using(connection, sqlDialect) ?: error("Expected DSL.using to not return null")
) : AbstractCoroutineContextElement(TransactionContext) {

    companion object Key : CoroutineContext.Key<TransactionContext> {
        val logger = logger<TransactionContext>()
    }

    val id = UUID.randomUUID()!!

    enum class Status {
        PENDING,
        IN_TRANSACTION,
        COMMITTED,
        ROLLED_BACK
    }

    var status: Status = Status.PENDING
        private set

    var isolationLevel: IsolationLevel = IsolationLevel.NONE
        private set

    var readOnly: Boolean = false
        private set

    private val beforeCommitHooks = mutableListOf<() -> Unit>()
    private val afterCommitHooks = mutableListOf<() -> Unit>()
    private val afterRollbackHooks = mutableListOf<() -> Unit>()
    private val afterCompletionHooks = mutableListOf<() -> Unit>()

    suspend fun <T> transaction(
        isolationLevel: IsolationLevel,
        readOnly: Boolean,
        block: suspend CoroutineScope.() -> T
    ): T = withContext(this) {
        beginTransaction(isolationLevel, readOnly)
        try {
            val result = block()
            commit()
            return@withContext result
        } catch (e: Throwable) {
            rollback()
            throw e
        }
    }

    private suspend fun beginTransaction(
        isolationLevel: IsolationLevel,
        readOnly: Boolean
    ) {
        execute(beforeOp = { if (status == Status.IN_TRANSACTION) error("Transaction has already been started.") }) {
            val readOnlyStr = if (readOnly) "ONLY" else "WRITE"
            val sql = "START TRANSACTION ISOLATION LEVEL ${isolationLevel.sqlStr} READ $readOnlyStr"
            dsl.execute(sql)
            logger.debug("${this@TransactionContext}: $sql")
        }
        this.status = Status.IN_TRANSACTION
        this.isolationLevel = isolationLevel
        this.readOnly = readOnly
    }

    private suspend fun commit() {
        beforeCommitHooks.forEach { it.invoke() }
        execute {
            logger.debug("${this@TransactionContext}: COMMIT")
            dsl.execute("COMMIT")
        }
        setCommitted()
        isolationLevel = IsolationLevel.NONE
        readOnly = false
        afterCommitHooks.forEach { it.invoke() }
        afterCompletionHooks.forEach { it.invoke() }
    }

    private suspend fun rollback() {
        execute {
            logger.debug("${this@TransactionContext}: ROLLBACK")
            dsl.execute("ROLLBACK")
        }
        setRolledBack()
        afterRollbackHooks.forEach { it.invoke() }
        afterCompletionHooks.forEach { it.invoke() }
    }

    private fun setCommitted() {
        status = Status.COMMITTED
        isolationLevel = IsolationLevel.NONE
        readOnly = false
    }

    private fun setRolledBack() {
        status = Status.ROLLED_BACK
        isolationLevel = IsolationLevel.NONE
        readOnly = false
    }

    suspend fun <T> execute(block: (DSLContext) -> T): T {
        return execute(
            beforeOp = {
                when (status) {
                    Status.PENDING -> error("Transaction has not been started")
                    Status.IN_TRANSACTION -> {
                        // Intentional no-op
                    }
                    Status.COMMITTED -> error("Transaction already committed")
                    Status.ROLLED_BACK -> error("Transaction already rolled back")
                }
            },
            block = block
        )
    }

    private suspend fun <T> execute(
        beforeOp: () -> Unit = {},
        block: (DSLContext) -> T
    ): T = withContext(Dispatchers.IO) {
        logger.trace("${this@TransactionContext}: Waiting for lock")
        try {
            synchronized(connection) {
                // Since waiting for the lock can take some time, check again that the job is not cancelled before executing `block`
                coroutineContext.ensureActive()
                logger.trace("${this@TransactionContext}: Acquired lock")
                beforeOp()
                block(dsl)
            }
        } finally {
            logger.trace("${this@TransactionContext}: Released lock")
        }
    }

    @Suppress("unused")
    fun beforeCommit(block: () -> Unit) = beforeCommitHooks.add(block)

    fun afterCommit(block: () -> Unit) = afterCommitHooks.add(block)

    @Suppress("unused")
    fun afterRollback(block: () -> Unit) = afterRollbackHooks.add(block)

    @Suppress("unused")
    fun afterCompletion(block: () -> Unit) = afterCompletionHooks.add(block)

    override fun toString(): String = "TransactionContext[$id]"
}
