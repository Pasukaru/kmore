package server

import io.ktor.application.Application
import io.ktor.application.featureOrNull
import io.ktor.application.install
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
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.singleton
import server.lib.controller.AbstractController
import server.lib.repository.Repositories
import server.lib.service.Services
import server.user.UserController
import server.user.UserRepository
import server.user.UserService
import java.util.*
import java.util.concurrent.TimeUnit

class KtorMain {

    companion object ApplicationContext {
        lateinit var kodein: Kodein
    }

    @KtorExperimentalLocationsAPI
    fun main(args: Array<String>) {
        embeddedServer(Netty, port = 8080) {
            kodein = Kodein {
                bindClass<UserRepository>()
                bind<Repositories>() with eagerSingleton { Repositories() }

                bindClass<UserService>()
                bind<Services>() with eagerSingleton { Services() }
            }

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

            listOf(UserController()).forEach {
                createControllerRouting(it)
            }
        }.gracefulStart()
    }

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

inline fun <reified CLASS : Any> Kodein.MainBuilder.bindClass() {
    val ctor = CLASS::class.constructors.find { it.parameters.isEmpty() }
        ?: throw java.lang.IllegalStateException("Cannot instanciate ${CLASS::class}: No default constructor")

    bind<CLASS>() with singleton { ctor.call() }
}