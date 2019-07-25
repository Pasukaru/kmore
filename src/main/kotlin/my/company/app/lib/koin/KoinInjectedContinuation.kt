package my.company.app.lib.koin

import org.koin.core.KoinApplication
import kotlin.coroutines.Continuation

class KoinInjectedContinuation<in T>(
    private val koinApplication: KoinApplication,
    private val continuation: Continuation<T>
) : Continuation<T> by continuation {
    override fun resumeWith(result: Result<T>) {
        KoinContext.KOIN.set(koinApplication)
        try {
            continuation.resumeWith(result)
        } finally {
            KoinContext.KOIN.set(koinApplication)
        }
    }
}
