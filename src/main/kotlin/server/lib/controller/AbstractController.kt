package server.lib.controller

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import server.inject
import server.lib.service.Services
import tx.transaction2Async

@KtorExperimentalLocationsAPI
abstract class AbstractController : Controller {

    val services: Services by inject()

    abstract val routing: Routing.() -> Unit

    protected suspend inline fun <RESPONSE : Any> PipelineContext<Unit, ApplicationCall>.jsonResponse(
        status: HttpStatusCode,
        crossinline op: suspend PipelineContext<Unit, ApplicationCall>.() -> RESPONSE
    ) {
        val result = op()
        call.response.status(status)
        call.respond(result)
    }

    protected inline fun <reified PATH : Any, RESPONSE : Any> Routing.jsonGet(
        status: HttpStatusCode = HttpStatusCode.OK,
        crossinline op: suspend PipelineContext<Unit, ApplicationCall>.() -> RESPONSE
    ) {
        get<PATH> { jsonResponse(status) { op() } }
    }

    protected inline fun <reified PATH : Any, RESPONSE : Any> Routing.jsonPut(
        status: HttpStatusCode = HttpStatusCode.OK,
        crossinline op: suspend PipelineContext<Unit, ApplicationCall>.() -> RESPONSE
    ) {
        put<PATH> { transaction2Async { jsonResponse(status) { op() } }.await() }
    }
}