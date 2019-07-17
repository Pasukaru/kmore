package my.company.app.lib.tx

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher

// This should probably have the same maxPoolSize as the (Hikari)ConnectionPool size plus
// the number of concurrent queries that are expected for transactions (x2 should be a good ballpark number?)
@UseExperimental(InternalCoroutinesApi::class)
@Suppress("MagicNumber")
object DbScheduler : ExperimentalCoroutineDispatcher(64, 128, "DB-IO") {
    override fun close() {
        throw UnsupportedOperationException("$this cannot be closed")
    }
}
