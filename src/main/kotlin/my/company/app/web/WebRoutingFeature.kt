package my.company.app.web

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.util.AttributeKey
import my.company.app.KtorMain
import my.company.app.lib.controller.AbstractController
import my.company.app.lib.controller.SwaggerController
import my.company.app.lib.instantiate
import my.company.app.lib.lazy
import my.company.app.lib.logger
import my.company.app.lib.swagger.SwaggerConfiguration
import my.company.app.web.interceptor.AuthInterceptor
import my.company.app.web.interceptor.CorsInterceptor
import org.koin.core.Koin
import org.koin.ktor.ext.getKoin
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

class WebRoutingFeature {
    private val swagger: SwaggerConfiguration by lazy()

    companion object Feature : ApplicationFeature<Application, Unit, WebRoutingFeature> {
        private val logger = logger<WebRoutingFeature>()
        private val KEY = AttributeKey<WebRoutingFeature>("WebRoutingFeature")
        override val key: AttributeKey<WebRoutingFeature> get() = KEY

        override fun install(pipeline: Application, configure: Unit.() -> Unit): WebRoutingFeature {
            return WebRoutingFeature().also { it.install(pipeline) }
        }

        val controllerClasses: List<KClass<out AbstractController>> by lazy {
            val start = System.currentTimeMillis()
            KtorMain.REFLECTIONS.getSubTypesOf(AbstractController::class.java)
                .asSequence()
                .filter { !Modifier.isAbstract(it.modifiers) }
                .map { it.kotlin }
                .toList()
                .also { logger.info("Scanning for controllers took ${System.currentTimeMillis() - start}ms") }
        }
    }

    private fun Routing.initControllers(koin: Koin) {
        controllerClasses.forEach { controllerType ->
            logger.info("Initializing controller: $controllerType")

            val instance = koin.instantiate(controllerType)
            this.apply(instance.routing)

            swagger.registerTag(instance.tag)
            instance.operations.forEach { swagger.registerApi(it) }
        }
    }

    private fun install(application: Application) = with(application) {
        val time = measureTimeMillis {
            val koin = getKoin()
            routing {
                CorsInterceptor().register(this, ApplicationCallPipeline.Call)
                AuthInterceptor().register(this, ApplicationCallPipeline.Call)
                initControllers(koin)
                koin.instantiate(SwaggerController::class).routing(this)
            }
        }
        logger.info("WebRoutingFeature initialized in ${time}ms")
    }
}
