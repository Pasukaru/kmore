package my.company.app.business_logic

import my.company.app.lib.AuthorizationService
import my.company.app.lib.inject
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import my.company.app.lib.tx.TransactionContext
import javax.validation.Validator
import kotlin.coroutines.coroutineContext

abstract class BusinessLogicAction<REQUEST, RESPONSE> {
    protected val logger = this::class.logger()
    protected val repo: Repositories by inject()
    protected val validator: Validator by inject()
    protected val authorizationService: AuthorizationService by inject()

    open suspend fun execute(request: REQUEST): RESPONSE {
        val start = System.currentTimeMillis()

        validator.validate(request)
        val response = action(request)

        val time = System.currentTimeMillis() - start
        logger.debug("${this::class.simpleName} completed int ${time}ms")

        return response
    }

    protected abstract suspend fun action(request: REQUEST): RESPONSE

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
