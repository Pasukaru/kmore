@file:Suppress("EXPERIMENTAL_API_USAGE")

package my.company.app

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.features.StatusPages
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import my.company.app.business_logic.session.SessionActions
import my.company.app.business_logic.user.UserActions
import my.company.app.conf.AppConfig
import my.company.app.conf.AppConfigLoader
import my.company.app.conf.AppConfigLoader.initLogging
import my.company.app.db.ModelGenerator
import my.company.app.lib.AuthorizationService
import my.company.app.lib.IdGenerator
import my.company.app.lib.PasswordHelper
import my.company.app.lib.TimeService
import my.company.app.lib.TransactionService
import my.company.app.lib.koin.containerModule
import my.company.app.lib.koin.eager
import my.company.app.lib.koin.withKoin
import my.company.app.lib.ktor.ApplicationWarmup
import my.company.app.lib.ktor.HikariCPFeature
import my.company.app.lib.ktor.KoinFeature
import my.company.app.lib.ktor.StartupLog
import my.company.app.lib.ktor.getKoin
import my.company.app.lib.ktor.uuidConverter
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import my.company.app.lib.swagger.SwaggerConfiguration
import my.company.app.lib.validation.ValidationService
import my.company.app.web.GlobalWebErrorHandler
import my.company.app.web.WebRoutingFeature
import org.koin.dsl.module
import org.reflections.Reflections
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.schema.ModelRef
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.validation.Validation

private lateinit var appConfig: AppConfig

class KtorMain {

    companion object {
        const val GRADE_PERIOD_IN_SECONDS = 1L
        const val SHUTDOWN_TIMEOUT_IN_SECONDS = 5L
        val REFLECTIONS: Reflections by lazy {
            val start = System.currentTimeMillis()
            val r = Reflections(PackageNoOp::class.java.`package`.name)
            logger.info("Classpath scanning took: ${System.currentTimeMillis() - start}ms")
            r
        }

        val logger = logger<KtorMain>()
    }

    fun main() {
        val config = initConfig(System.getenv("PROFILE"))
        initLogging(config)
        embeddedServer(
            Netty,
            port = appConfig.ktorPort,
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
}

fun initConfig(profile: String? = null): AppConfig {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
    appConfig = AppConfigLoader.loadProfile(profile)
    return appConfig
}

fun Application.mainModule() {
    install(CallLogging)
    install(Locations)

    install(KoinFeature) {
        modules(
            listOf(
                module {
                    single { appConfig }
                    single {
                        val authHeader = ParameterBuilder()
                            .name("X-Auth-Token")
                            .description("Authentication token")
                            .modelRef(ModelRef("uuid"))
                            .parameterType("header")
                            .required(false)
                            .allowEmptyValue(false)
                            .allowMultiple(false)
                            .build()
                            .let { listOf(it) }
                        SwaggerConfiguration()
                            .registerOperationParameterInterceptor { authHeader }
                    }
                    single { IdGenerator() }
                    single { TimeService() }
                    single { ModelGenerator() }
                    single { AuthorizationService() }
                    single { GlobalWebErrorHandler() }
                    single { PasswordHelper() }
                    val validationFactory = Validation.buildDefaultValidatorFactory()!!
                    single { validationFactory }
                    single { validationFactory.validator }
                    single { ValidationService() }
                    single { TransactionService() }
                },
                containerModule<Repositories>(),
                containerModule<SessionActions>(),
                containerModule<UserActions>()
            )
        )
    }

    install(DataConversion) {
        uuidConverter()
    }

    install(ContentNegotiation) {
        jackson {
            configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Instead of throwing an exception, ignore additional json properties that don't exist in our DTOs
            getKoin().modules(module { single(createdAtStart = true) { this@jackson } })
        }
    }

    install(HikariCPFeature)

    install(WebRoutingFeature)
    install(StatusPages) {
        exception<Throwable> { error ->
            withKoin {
                val errorHandler = eager<GlobalWebErrorHandler>()
                if (!errorHandler.handleError(this@exception, error)) {
                    KtorMain.logger.error("Caught unhandled error:", error)
                }
            }
        }
    }

    if (!appConfig.isDev && !appConfig.isTest) {
        install(ApplicationWarmup)
    }

    install(StartupLog)
}

fun main() = KtorMain().main()
