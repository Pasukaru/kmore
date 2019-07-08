@file:Suppress("EXPERIMENTAL_API_USAGE")

package my.company.app

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStarted
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.AttributeKey
import my.company.app.lib.controller.Controller
import my.company.app.lib.ktor.uuidConverter
import my.company.app.lib.repository.Repositories
import my.company.app.lib.service.Services
import org.koin.core.KoinApplication
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.getKoin
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.lang.reflect.Modifier
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit


private class KtorMain {
    fun main() {
        embeddedServer(
            Netty,
            port = 8080,
            module = Application::mainModule
        ).gracefulStart()
    }

    private fun <T : ApplicationEngine> T.gracefulStart(): T {
        this.start(wait = false)
        Runtime.getRuntime().addShutdownHook(Thread({
            this.stop(1, 5, TimeUnit.SECONDS)
        }, "KtorMain"))
        Thread.currentThread().join()
        return this
    }
}

private object StartupLog : ApplicationFeature<Application, KoinApplication, Koin> {
    private val logger = LoggerFactory.getLogger(StartupLog::class.java)

    override val key: AttributeKey<Koin>
        get() = AttributeKey("KtorMain.StartupLog")

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

private fun Application.mainModule() {
    install(CallLogging)
    install(Locations)
    install(DataConversion) {
        uuidConverter()
    }

    install(ContentNegotiation) {
        jackson {}
    }

    install(Koin) {
        modules(
            listOf(
                Repositories.MODULE,
                Services.MODULE
            )
        )
    }

    install(Routing).initControllers()

    install(StartupLog)
}

private fun Routing.initControllers() {
    val logger = LoggerFactory.getLogger(PackageNoOp::class.java.`package`.name + ".Routing")
    val koin = this.application.getKoin()

    val start = Instant.now()
    val controllers = Reflections(PackageNoOp::class.java.`package`.name).getSubTypesOf(Controller::class.java)
        .asSequence()
        .filter { !Modifier.isAbstract(it.modifiers) }
        .map { it.kotlin }
        .toList()

    logger.info("Classpath scanning took: ${Duration.between(start, Instant.now())}")

    controllers
        .forEach { controllerType ->
            logger.info("Initializing controller: $controllerType")
            val instance = koin.instanciate(controllerType)
            this.apply(instance.routing)
        }
}

fun main() = KtorMain().main()