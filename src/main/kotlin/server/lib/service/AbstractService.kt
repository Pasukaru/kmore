package server.lib.service

import io.ktor.locations.KtorExperimentalLocationsAPI
import org.kodein.di.generic.instance
import server.KtorMain
import server.lib.repository.Repositories

abstract class AbstractService : Service {
    override val kodein by lazy { KtorMain.kodein }
    val repo: Repositories by instance()
    @KtorExperimentalLocationsAPI
    val svc: Services by instance()
}