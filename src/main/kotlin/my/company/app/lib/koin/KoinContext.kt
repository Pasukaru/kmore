package my.company.app.lib.koin

import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import java.lang.Thread.currentThread

object KoinContext : ThreadLocal<KoinApplication?>() {

    fun getOrError() = get() ?: error("KoinApplication has not been started in thread ${currentThread().name}")

    fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication {
        val koinApplication = KoinApplication.create()
        appDeclaration(koinApplication)
        koinApplication.createEagerInstances()
        set(koinApplication)
        return koinApplication
    }

    fun stopKoin() = with(get()) {
        getOrError().close()
        set(null)
    }
}
