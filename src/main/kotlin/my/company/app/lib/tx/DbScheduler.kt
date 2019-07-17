package my.company.app.lib.tx

import ch.qos.logback.core.CoreConstants.CORE_POOL_SIZE
import ch.qos.logback.core.CoreConstants.MAX_POOL_SIZE
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher

// This should probably have the same maxPoolSize as the (Hikari)ConnectionPool size plus
// the number of concurrent queries that are expected for transactions (x2 should be a good ballpark number?)
@UseExperimental(InternalCoroutinesApi::class)
object DbScheduler : ExperimentalCoroutineDispatcher(CORE_POOL_SIZE, MAX_POOL_SIZE, "DB-IO") {
    private const val CORE_POOL_SIZE = 64
    private const val MAX_POOL_SIZE = 128
    override fun close() {
        throw UnsupportedOperationException("$this cannot be closed")
    }
}
