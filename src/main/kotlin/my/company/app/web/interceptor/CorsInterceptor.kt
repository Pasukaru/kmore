package my.company.app.web.interceptor

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.request.httpMethod
import io.ktor.response.header
import io.ktor.util.pipeline.PipelineContext

object CorsInterceptor : WebInterceptor() {
    override suspend fun PipelineContext<*, ApplicationCall>.intercept() {
        if (call.request.httpMethod != HttpMethod.Options) return
        call.response.header("Access-Control-Allow-Origin", "*")
        call.response.header("Access-Control-Allow-Credentials", "true")
        call.response.header("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT,DELETE")
        call.response.header(
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, X-Auth-Token, Date"
        )
        call.response.header("Access-Control-Expose-Headers", "Date")
    }
}
