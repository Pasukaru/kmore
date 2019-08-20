package my.company.app.web.interceptor

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthenticationCredentials
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import my.company.app.conf.AppConfig
import my.company.app.lib.DatabaseService
import my.company.app.lib.koin.lazy
import my.company.app.lib.logger
import my.company.app.lib.tryOrNull
import my.company.app.web.AuthenticatedUser
import my.company.app.web.SessionKey
import my.company.app.web.auth.WebSessionPrincipal
import my.company.app.web.isSwaggerRequest
import my.company.app.web.isWebRequest
import java.util.UUID

@Suppress("EXPERIMENTAL_API_USAGE")
class AuthInterceptor : WebInterceptor() {

    private val logger = logger<AuthInterceptor>()
    private val appConfig: AppConfig by lazy()
    private val databaseService: DatabaseService by lazy()

    override suspend fun PipelineContext<*, ApplicationCall>.intercept() {
        val start = System.currentTimeMillis()

        if (isSwaggerRequest()) {
            if (call.request.basicAuthenticationCredentials(Charsets.UTF_8) != appConfig.swaggerBasicAuthCredential) {
                call.response.header(HttpHeaders.WWWAuthenticate, "Basic realm=\"swagger\"")
                call.respond(HttpStatusCode.Unauthorized)
                finish()
            }
        }

        if (isWebRequest()) {
            setSessionCallAttributes()
        }

        if (logger.isTraceEnabled) {
            val time = System.currentTimeMillis() - start
            logger.trace("Auth interceptor completed in ${time}ms. (${call.request.httpMethod.value} ${call.request.path()})")
        }
    }

    private suspend fun PipelineContext<*, ApplicationCall>.setSessionCallAttributes() {
        databaseService.noTransaction {
            val token = call.request.header("X-Auth-Token")?.tryOrNull { UUID.fromString(it) }

            if (token != null) {
                val sessionUser = repositories.session.getSessionByIdWithUser(token)
                if (sessionUser != null) {
                    val (session, user) = sessionUser
                    call.attributes.put(SessionKey, session)
                    call.attributes.put(AuthenticatedUser, user)
                    call.authentication.principal = WebSessionPrincipal(
                        session = WebSessionPrincipal.Session(
                            session.id
                        ),
                        user = WebSessionPrincipal.User(
                            user.id
                        )
                    )
                    logger.debug("CurrentUser: ${user.firstName}")
                }
            }

            if (call.authentication.principal == null) {
                logger.debug("Not logged in.")
            }
        }
    }
}
