package my.company.app.lib.koin

import org.koin.core.KoinApplication
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class KoinCoroutineInterceptor(
    private val koin: KoinApplication
) : ContinuationInterceptor {
    companion object Key : CoroutineContext.Key<KoinCoroutineInterceptor>

    override val key: CoroutineContext.Key<*> = Key
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return KoinInjectedContinuation(koin, continuation)
    }
}
