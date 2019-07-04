import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher

@UseExperimental(InternalCoroutinesApi::class)
internal object IOScheduler : ExperimentalCoroutineDispatcher(64, 128, "IOScheduler") {
    override fun close() {
        throw UnsupportedOperationException("$this cannot be closed")
    }
}