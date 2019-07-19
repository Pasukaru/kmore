@file:Suppress("EXPERIMENTAL_API_USAGE")

package my.company.app

import com.fasterxml.jackson.annotation.JsonInclude
import de.nielsfalk.ktor.swagger.SwaggerSupport
import de.nielsfalk.ktor.swagger.version.shared.Contact
import de.nielsfalk.ktor.swagger.version.shared.Information
import de.nielsfalk.ktor.swagger.version.v2.Swagger
import de.nielsfalk.ktor.swagger.version.v3.OpenApi
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.db.jooq.HikariCPFeature
import my.company.app.lib.ktor.ApplicationWarmup
import my.company.app.lib.ktor.StartupLog
import my.company.app.lib.ktor.uuidConverter
import my.company.app.lib.ktor.web.GlobalWebErrorHandler
import my.company.app.lib.ktor.web.WebRouting
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.reflections.Reflections
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class KtorMain {

    companion object {
        const val GRADE_PERIOD_IN_SECONDS = 1L
        const val SHUTDOWN_TIMEOUT_IN_SECONDS = 5L
        val REFLECTIONS: Reflections

        val logger = logger<KtorMain>()

        init {
            val start = Instant.now()
            REFLECTIONS = Reflections(PackageNoOp::class.java.`package`.name)
            logger.info("Classpath scanning took: ${Duration.between(start, Instant.now())}")
        }
    }

    fun main() {
        embeddedServer(
            Netty,
            port = 8080,
            module = { mainModule(this) }
        ).gracefulStart()
    }

    private fun <T : ApplicationEngine> T.gracefulStart(): T {
        this.start(wait = false)
        Runtime.getRuntime().addShutdownHook(Thread({
            this.stop(GRADE_PERIOD_IN_SECONDS, SHUTDOWN_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        }, "KtorMain"))
        Thread.currentThread().join()
        return this
    }

    private fun mainModule(application: Application) = with(application) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
        install(CallLogging)
        install(Locations)

        install(Koin) {
            modules(
                listOf(
                    module { single { GlobalWebErrorHandler() } },
                    Repositories.MODULE,
                    SessionActions.MODULE,
                    UserActions.MODULE
                )
            )
        }

        install(DataConversion) {
            uuidConverter()
        }

        install(ContentNegotiation) {
            jackson {
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                GlobalContext.get().modules(module { single { this@jackson } })
            }
        }

        install(HikariCPFeature)

        install(SwaggerSupport) {
            forwardRoot = true
            val information = Information(
                version = "0.1",
                title = "sample api implemented in ktor",
                description = "This is a sample which combines [ktor](https://github.com/Kotlin/ktor) with [swaggerUi](https://swagger.io/). You find the sources on [github](https://github.com/nielsfalk/ktor-swagger)",
                contact = Contact(
                    name = "Niels Falk",
                    url = "https://nielsfalk.de"
                )
            )
            swagger = Swagger().apply {
                info = information
                definitions["UUID"] = mutableMapOf(
                    "type" to "string",
                    "name" to "UUID"
                )
            }
            openApi = OpenApi().apply {
                info = information
                components.schemas["UUID"] = mutableMapOf(
                    "type" to "string",
                    "name" to "UUID"
                )
            }
        }

        install(WebRouting)

        install(ApplicationWarmup)

        install(StartupLog)
    }

}

fun main() = KtorMain().main()
