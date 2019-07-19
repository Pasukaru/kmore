package my.company.app.lib.ktor.web.interceptor

import io.ktor.application.ApplicationCall
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelinePhase
import my.company.app.lib.inject
import my.company.app.lib.repository.Repositories

abstract class WebInterceptor {
    protected val repositories: Repositories by inject()

    fun register(routing: Routing, phase: PipelinePhase) {
        routing.intercept(phase) { intercept() }
    }

    protected abstract suspend fun PipelineContext<*, ApplicationCall>.intercept()
}
