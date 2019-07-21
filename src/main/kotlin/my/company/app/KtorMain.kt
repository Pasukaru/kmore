@file:Suppress("EXPERIMENTAL_API_USAGE")

package my.company.app

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
import io.ktor.features.StatusPages
import io.ktor.locations.Locations
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.conf.AppConfigFeature
import my.company.app.db.jooq.HikariCPFeature
import my.company.app.lib.AuthorizationService
import my.company.app.lib.PasswordHelper
import my.company.app.lib.eager
import my.company.app.lib.ktor.ApplicationWarmup
import my.company.app.lib.ktor.StartupLog
import my.company.app.lib.ktor.uuidConverter
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import my.company.app.web.GlobalWebErrorHandler
import my.company.app.web.WebRouting
import my.company.app.web.jacksonWeb
import my.company.app.web.swagger
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.reflections.Reflections
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.validation.Validation

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
            module = Application::mainModule
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

    // private val mainModule: Application.() -> Unit =

}

fun Application.mainModule() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))

    val appConfig = install(AppConfigFeature)
    install(CallLogging)
    install(Locations)

    install(Koin) {
        modules(
            listOf(
                module {
                    single { appConfig }
                    single { AuthorizationService() }
                    single { GlobalWebErrorHandler() }
                    single { PasswordHelper() }
                    val validationFactory = Validation.buildDefaultValidatorFactory()!!
                    single { validationFactory }
                    single { validationFactory.validator }
                },
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
        jacksonWeb()
        swagger()
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
    install(StatusPages) {
        exception<Throwable> { error ->
            if (!eager<GlobalWebErrorHandler>().handleError(this, error)) {
                KtorMain.logger.error("Caught unhandled error:", error)
            }
        }
    }

    install(ApplicationWarmup)

    install(StartupLog)
}

fun main() = KtorMain().main()
