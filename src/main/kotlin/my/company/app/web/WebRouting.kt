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
import my.company.app.lib.eager
import my.company.app.lib.instantiate
import my.company.app.lib.logger
import my.company.app.lib.swagger.SwaggerConfiguration
import my.company.app.web.interceptor.AuthInterceptor
import my.company.app.web.interceptor.CorsInterceptor
import org.koin.core.Koin
import org.koin.ktor.ext.getKoin
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

object WebRouting : ApplicationFeature<Application, Unit, Unit> {
    private val logger = logger<WebRouting>()

    override val key: AttributeKey<Unit>
        get() = AttributeKey(this::class.java.canonicalName)

    private fun controllerClasses(): List<KClass<out AbstractController>> {
        return KtorMain.REFLECTIONS.getSubTypesOf(AbstractController::class.java)
            .asSequence()
            .filter { !Modifier.isAbstract(it.modifiers) }
            .map { it.kotlin }
            .toList()
    }

    private fun Routing.initControllers(koin: Koin) {
        val swagger = eager<SwaggerConfiguration>()

        controllerClasses()
            .forEach { controllerType ->
                KtorMain.logger.info("Initializing controller: $controllerType")
                val instance = koin.instantiate(controllerType)
                this.apply(instance.routing)

                swagger.registerTag(instance.tag)
                instance.operations.forEach { swagger.registerApi(it) }
            }
    }

    override fun install(pipeline: Application, configure: Unit.() -> Unit) {
        val time = measureTimeMillis {
            val koin = pipeline.getKoin()

            pipeline.routing {
                CorsInterceptor.register(this, ApplicationCallPipeline.Call)
                AuthInterceptor.register(this, ApplicationCallPipeline.Call)
                initControllers(koin)
                koin.instantiate(SwaggerController::class).routing(this)
            }
        }
        logger.trace("WebRouting initialized in ${time}ms")
    }
}
