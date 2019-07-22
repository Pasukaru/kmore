package my.company.app.lib.ktor

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStarted
import io.ktor.util.AttributeKey
import my.company.app.lib.logger
import java.lang.management.ManagementFactory
import java.time.Duration
import java.util.Date

object StartupLog : ApplicationFeature<Application, Unit, StartupLog> {
    private val KEY = AttributeKey<StartupLog>(this::class.java.canonicalName)
    override val key: AttributeKey<StartupLog> get() = KEY

    private val logger = logger<StartupLog>()

    override fun install(pipeline: Application, configure: Unit.() -> Unit): StartupLog {
        pipeline.environment.monitor.subscribe(ApplicationStarted) {
            Date(ManagementFactory.getRuntimeMXBean().startTime)
            val duration = Duration.ofMillis(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().startTime)
            logger.info("Server started in ${duration.toMinutesPart()}m ${duration.toSecondsPart()}.${duration.toMillisPart()}s.")
        }
        return StartupLog
    }
}
