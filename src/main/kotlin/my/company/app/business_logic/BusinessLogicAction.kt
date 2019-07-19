package my.company.app.business_logic

import my.company.app.lib.inject
import my.company.app.lib.repository.Repositories
import my.company.app.lib.tx.TransactionContext
import kotlin.coroutines.coroutineContext

abstract class BusinessLogicAction<REQUEST, RESPONSE> {
    protected val repo: Repositories by inject()

    abstract suspend fun execute(request: REQUEST): RESPONSE

    @Suppress("MemberVisibilityCanBePrivate")
    protected suspend inline fun currentTransaction(): TransactionContext? = coroutineContext[TransactionContext]

    protected suspend fun afterCommit(block: () -> Unit, immediateOutsideOfTransaction: Boolean) {
        val transaction = currentTransaction()
        if (transaction != null) {
            transaction.afterCommit(block)
            return
        }

        if (immediateOutsideOfTransaction) block()
        else throw IllegalStateException("Not in a transaction")
    }
}
