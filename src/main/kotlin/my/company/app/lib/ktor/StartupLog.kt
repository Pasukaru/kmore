package my.company.app.lib.ktor

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStarted
import io.ktor.util.AttributeKey
import my.company.app.lib.logger
import org.koin.core.KoinApplication
import org.koin.ktor.ext.Koin
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant
import java.util.Date

object StartupLog : ApplicationFeature<Application, KoinApplication, Koin> {
    private val logger = logger<StartupLog>()

    override val key: AttributeKey<Koin>
        get() = AttributeKey(this::class.java.canonicalName)

    override fun install(pipeline: Application, configure: (KoinApplication).() -> Unit): Koin {
        pipeline.environment.monitor.subscribe(ApplicationStarted) {
            Date(ManagementFactory.getRuntimeMXBean().startTime)
            val duration =
                Duration.ofMillis(ManagementFactory.getRuntimeMXBean().startTime - Instant.now().toEpochMilli())
                    .abs()
            logger.info("Server started in ${duration.toMinutesPart()}m ${duration.toSecondsPart()}.${duration.toMillisPart()}s.")
        }
        return Koin()
    }
}
