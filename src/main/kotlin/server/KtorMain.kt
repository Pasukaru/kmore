package server

import io.ktor.application.Application
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.DataConversionException
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import server.lib.controller.AbstractController
import server.lib.repository.Repositories
import server.lib.service.Services
import server.user.UserController
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class KtorMain {
    @KtorExperimentalLocationsAPI
    fun main(args: Array<String>) {
        embeddedServer(Netty, port = 8080) {
            install(CallLogging)
            install(Locations)
            install(DataConversion) {
                convert<UUID> {
                    decode { values, _ -> values.singleOrNull()?.let(UUID::fromString) }
                    encode { value ->
                        when (value) {
                            null -> listOf()
                            is UUID -> listOf(value.toString())
                            else -> throw DataConversionException("Cannot convert $value to UUID")
                        }
                    }
                }
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

            listOf(UserController()).forEach {
                createControllerRouting(it)
            }
        }.gracefulStart()
    }

    @KtorExperimentalLocationsAPI
    private fun Application.createControllerRouting(controller: AbstractController): Routing {
        return featureOrNull(Routing)?.apply(controller.routing) ?: install(Routing, controller.routing)
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

@KtorExperimentalLocationsAPI
fun main(args: Array<String>) = KtorMain().main(args)

inline fun <reified T : Any> inject(qualifier: Qualifier? = null): Lazy<T> {
    return lazy { GlobalContext.get().koin.get<T>(T::class, qualifier, null) }
}

fun <T : Any> Scope.createInstance(
    clazz: KClass<T>
): T {
    val ctor =
        clazz.primaryConstructor ?: throw IllegalStateException("Cannot instanciate class without primary ctor: $clazz")

    val params = ctor.parameters.associateWith {
        val paramClass = it.type.classifier as? KClass<*>
            ?: throw IllegalStateException("Cannot instanciate class with unclassified primary ctor parameters: $clazz $ctor")
        get<Any>(paramClass, null, null)
    }

    params.forEach {
        println(it.value::class.java)
    }

    val instance = ctor.callBy(params)

    return instance
}

inline fun <reified T : Any> KClass<T>.containerModule(): Module = module { singleContainer<T>() }
inline fun <reified T : Any> org.koin.core.module.Module.singleContainer(options: Options = Options()) {
    T::class.declaredMemberProperties.forEach {
        val clazz = it.returnType.classifier as KClass<*>
        singleInstance(clazz)
    }

    singleInstance<T>()
}

inline fun <reified T : Any> org.koin.core.module.Module.singleInstance(options: Options = Options()) =
    singleInstance(T::class, options)

fun <T : Any> org.koin.core.module.Module.singleInstance(clazz: KClass<T>, options: Options = Options()) {
    val bean = BeanDefinition<Any>(null, null, clazz)
    bean.definition = { createInstance(clazz) }
    bean.kind = Kind.Single
    this.declareDefinition(bean, options)
}