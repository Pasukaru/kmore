package my.company.app.web.interceptor

import io.ktor.application.ApplicationCall
import io.ktor.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelinePhase
import my.company.app.lib.koin.lazy
import my.company.app.lib.repository.Repositories

abstract class WebInterceptor {
    protected val repositories: Repositories by lazy()

    fun register(routing: Routing, phase: PipelinePhase) {
        routing.intercept(phase) { intercept() }
    }

    protected abstract suspend fun PipelineContext<*, ApplicationCall>.intercept()
}
