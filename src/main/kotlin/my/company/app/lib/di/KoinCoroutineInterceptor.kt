package my.company.app.lib.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

object KoinContext {
    val KOIN = ThreadLocal<KoinApplication>()
    val koinApplication: KoinApplication get() = KOIN.get() ?: error("KoinApplication has not been started")
    val koin: Koin get() = koinApplication.koin
    val koinOrNull: Koin? get() = KOIN.get()?.koin

    fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication {
        val koinApplication = KoinApplication.create()
        appDeclaration(koinApplication)
        koinApplication.createEagerInstances()
        KOIN.set(koinApplication)
        return koinApplication
    }

    fun stopKoin() {
        val koin = KOIN.get() ?: error("Koin application not started")
        koin.close()
        KOIN.set(null)
    }
}

private class KoinInjectedContinuation<in T>(
    private val koin: KoinApplication,
    private val continuation: Continuation<T>
) : Continuation<T> by continuation {
    override fun resumeWith(result: Result<T>) {
        KoinContext.KOIN.set(koin)
        continuation.resumeWith(result)
    }
}

class KoinCoroutineInterceptor(
    private val koin: KoinApplication
) : ContinuationInterceptor {
    companion object Key : CoroutineContext.Key<KoinCoroutineInterceptor>

    override val key: CoroutineContext.Key<*> = Key
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        KoinContext.KOIN.set(koin)
        return KoinInjectedContinuation(koin, continuation)
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        KoinContext.KOIN.set(null)
    }
}
