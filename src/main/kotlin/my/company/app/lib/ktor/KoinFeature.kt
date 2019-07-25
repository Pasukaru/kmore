package my.company.app.lib.ktor

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStopped
import io.ktor.application.feature
import io.ktor.util.AttributeKey
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.withKoin
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration

class KoinFeature(val koinApplication: KoinApplication) {
    companion object Feature : ApplicationFeature<Application, KoinApplication, KoinFeature> {
        private val KEY: AttributeKey<KoinFeature> = AttributeKey("Koin")
        override val key: AttributeKey<KoinFeature> = KEY

        override fun install(pipeline: Application, configure: KoinAppDeclaration): KoinFeature {
            val koin = KoinApplication.create()

            pipeline.environment.monitor.subscribe(ApplicationStopped) {
                koin.close()
            }

            configure(koin)
            koin.createEagerInstances()
            KoinContext.KOIN.set(koin)

            pipeline.intercept(ApplicationCallPipeline.Call) {
                withKoin { proceed() }
            }

            return KoinFeature(koin)
        }
    }
}

fun Application.getKoin(): KoinApplication = feature(KoinFeature).koinApplication
