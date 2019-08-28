package my.company.app.business_logic

import my.company.app.db.ModelGenerator
import my.company.app.lib.AuthorizationService
import my.company.app.lib.DatabaseService
import my.company.app.lib.TransactionContext
import my.company.app.lib.koin.lazy
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import my.company.app.lib.validation.ValidationService
import kotlin.coroutines.coroutineContext

@Suppress("MemberVisibilityCanBePrivate")
abstract class BusinessLogicAction<REQUEST : Any, RESPONSE>(
    protected val transactionOptions: TransactionOptions = TransactionOptions.DEFAULT
) {
    protected val logger = this::class.logger()
    protected val repo: Repositories by lazy()
    protected val validator: ValidationService by lazy()
    protected val generate: ModelGenerator by lazy()
    protected val authorizationService: AuthorizationService by lazy()
    protected val databaseService: DatabaseService by lazy()

    open suspend fun execute(request: REQUEST): RESPONSE {
        val start = System.currentTimeMillis()

        val validatedRequest = validate(request)
        val response = runInTransaction(validatedRequest)

        val time = System.currentTimeMillis() - start
        logger.debug("${this::class.simpleName} completed in ${time}ms")

        return response
    }

    protected abstract suspend fun action(request: REQUEST): RESPONSE

    protected fun <T : Any> validate(obj: T): T = validator.validate(obj)

    protected suspend inline fun currentTransaction(): TransactionContext? = coroutineContext[TransactionContext]

    protected suspend fun runInTransaction(request: REQUEST): RESPONSE {
        return databaseService.transaction(
            isolationLevel = transactionOptions.isolationLevel,
            readOnly = transactionOptions.readOnly,
            inherit = transactionOptions.inherit
        ) { action(request) }
    }

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
