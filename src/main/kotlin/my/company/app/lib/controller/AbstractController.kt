package my.company.app.lib.controller

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import my.company.app.lib.tx.transactionAsync

abstract class AbstractController : Controller {

    protected suspend inline fun PipelineContext<Unit, ApplicationCall>.jsonResponse(
        status: HttpStatusCode,
        body: Any
    ) {
        call.response.status(status)
        call.respond(body)
    }

    protected inline fun <RESPONSE : Any> Routing.jsonPost(
        path: String,
        status: HttpStatusCode = HttpStatusCode.Created,
        crossinline op: suspend PipelineContext<Unit, ApplicationCall>.() -> RESPONSE
    ) {
        post(path) {
            try {
                val response = transactionAsync { op() }.await()
                jsonResponse(status, response)
            } catch (e: Throwable) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
