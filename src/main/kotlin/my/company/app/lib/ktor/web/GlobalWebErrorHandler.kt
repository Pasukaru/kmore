package my.company.app.lib.ktor.web

import getEndpointInformation
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import my.company.app.business_logic.session.InvalidLoginCredentialsException
import my.company.app.lib.logger

class GlobalWebErrorHandler {

    val logger = logger<GlobalWebErrorHandler>()

    class DtoError(val message: String)

    suspend fun PipelineContext<Unit, ApplicationCall>.badRequest(e: Throwable) {
        logger.error("Caught global error from endpoint: ${this.getEndpointInformation()}", e)
        call.respond(HttpStatusCode.BadRequest, DtoError(e.message ?: e.javaClass.simpleName))
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.internalServerError(e: Throwable) {
        logger.error("Caught global error from endpoint: ${this.getEndpointInformation()}", e)
        call.respond(HttpStatusCode.InternalServerError, DtoError(HttpStatusCode.InternalServerError.description))
    }

    suspend fun handleError(context: PipelineContext<Unit, ApplicationCall>, e: Throwable): Unit = with(context) {
        when (e) {
            is InvalidLoginCredentialsException -> badRequest(e)
            else -> internalServerError(e)
        }
    }
}
