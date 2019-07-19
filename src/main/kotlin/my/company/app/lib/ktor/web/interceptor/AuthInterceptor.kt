package my.company.app.lib.ktor.web.interceptor

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.util.pipeline.PipelineContext
import my.company.app.lib.ktor.web.AuthenticatedUser
import my.company.app.lib.ktor.web.SessionKey
import my.company.app.lib.logger
import my.company.app.lib.tryOrNull
import my.company.app.lib.tx.withoutTransaction
import java.util.UUID

object AuthInterceptor : WebInterceptor() {

    private val logger = logger<AuthInterceptor>()

    override suspend fun PipelineContext<*, ApplicationCall>.intercept(): Unit = withoutTransaction {
        val token = call.request.header("X-Auth-Token")?.tryOrNull { UUID.fromString(it) }
        if (token != null) {
            val session = repositories.session.findById(token)
            if (session != null) {
                call.attributes.put(SessionKey, session)
                val user = repositories.user.findById(session.userId)
                if (user != null) {
                    call.attributes.put(AuthenticatedUser, user)
                    logger.debug("CurrentUser: ${user.firstName}")
                    return@withoutTransaction
                }
            }
        }
        logger.debug("Not logged in.")
    }
}
