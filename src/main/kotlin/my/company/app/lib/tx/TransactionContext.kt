package my.company.app.lib.tx

import com.zaxxer.hikari.pool.HikariPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import my.company.app.inject
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class TransactionContext(
    val connection: Connection,
    val dialect: SQLDialect = SQLDialect.POSTGRES_9_5
) : AbstractCoroutineContextElement(TransactionContext) {

    companion object Key : CoroutineContext.Key<TransactionContext> {
        val logger = LoggerFactory.getLogger(TransactionContext::class.java)!!
    }

    val id = UUID.randomUUID()!!

    val mutex: Mutex = Mutex()
    var committed: Boolean = false
        private set

    var rolledBack: Boolean = false
        private set

    suspend fun beginTransaction() {
        execute {
            logger.trace("$this: BEGIN")
            DSL.using(connection, dialect).execute("BEGIN TRANSACTION")
        }
    }

    suspend fun commit() {
        execute {
            logger.trace("$this: COMMIT")
            DSL.using(connection, dialect).execute("COMMIT")
            committed = true
        }
    }

    suspend fun rollback() {
        execute {
            logger.trace("$this: ROLLBACK")
            DSL.using(connection, dialect).execute("ROLLBACK")
            rolledBack = true
        }
    }

    fun expectActive() {
        if (committed || rolledBack) throw IllegalStateException("Transaction is already completed")
    }

    suspend fun <T> execute(op: suspend Connection.() -> T): T {
        logger.trace("$this: LOCK ${coroutineContext[TransactionContext]}")
        mutex.lock()
        logger.trace("$this: LOCKED ${coroutineContext[TransactionContext]}")

        return try {
            expectActive()
            op(connection)
        } finally {
            logger.trace("$this: UNLOCKED ${coroutineContext[TransactionContext]}")
            mutex.unlock()
        }
    }

    override fun toString(): String = "TransactionContext[$id]"
}

fun <T> CoroutineScope.transactionAsync(block: suspend CoroutineScope.() -> T): Deferred<T> {
    // Use DB IO thread to await connection and begin transaction
    return async(DbScheduler) {
        val pool by inject<HikariPool>()

        val tx = TransactionContext(pool.connection)

        try {
            tx.beginTransaction()

            // Switch back to main threads for the transaction
            val result = async(Dispatchers.Default + tx, CoroutineStart.DEFAULT, block).await()

            tx.commit()

            result
        } catch (e: Throwable) {
            tx.rollback()
            throw e
        } finally {
            pool.evictConnection(tx.connection)
        }
    }
}
