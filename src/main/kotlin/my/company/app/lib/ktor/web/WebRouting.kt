package my.company.app.lib.ktor.web

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.util.AttributeKey
import my.company.app.KtorMain
import my.company.app.lib.controller.Controller
import my.company.app.lib.instantiate
import my.company.app.lib.ktor.web.interceptor.AuthInterceptor
import my.company.app.lib.ktor.web.interceptor.CorsInterceptor
import my.company.app.lib.logger
import org.koin.ktor.ext.getKoin
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

object WebRouting : ApplicationFeature<Application, Unit, Unit> {
    private val logger = logger<WebRouting>()

    override val key: AttributeKey<Unit>
        get() = AttributeKey(this::class.java.canonicalName)

    private fun controllerClasses(): List<KClass<out Controller>> {
        return KtorMain.REFLECTIONS.getSubTypesOf(Controller::class.java)
            .asSequence()
            .filter { !Modifier.isAbstract(it.modifiers) }
            .map { it.kotlin }
            .toList()
    }

    private fun Routing.initControllers() {
        val koin = this.application.getKoin()

        controllerClasses()
            .forEach { controllerType ->
                KtorMain.logger.info("Initializing controller: $controllerType")
                val instance = koin.instantiate(controllerType)
                this.apply(instance.routing)
            }
    }

    override fun install(pipeline: Application, configure: Unit.() -> Unit) {
        val time = measureTimeMillis {
            pipeline.routing {
                CorsInterceptor.register(this, ApplicationCallPipeline.Call)
                AuthInterceptor.register(this, ApplicationCallPipeline.Call)
                initControllers()
            }
        }
        logger.debug("WebRouting initialized in ${time}ms")
    }
}
