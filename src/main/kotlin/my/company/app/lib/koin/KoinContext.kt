package my.company.app.lib.koin

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration

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
