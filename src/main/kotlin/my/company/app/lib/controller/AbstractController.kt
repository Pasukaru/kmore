package my.company.app.lib.controller

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import my.company.app.inject
import my.company.app.lib.service.Services
import my.company.app.lib.tx.transaction2Async

@KtorExperimentalLocationsAPI
abstract class AbstractController : Controller {

    protected val services: Services by inject()

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