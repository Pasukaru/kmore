package my.company.app.web.auth

import io.ktor.auth.Principal
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class WebSessionPrincipal(
    val session: Session,
    val user: User
) : AbstractCoroutineContextElement(WebSessionPrincipal), Principal {
    data class Session(
        val id: UUID
    )

    data class User(
        val id: UUID,
        val permissions: List<String> = listOf("USERS_CAN_READ")
    )

    companion object Key : CoroutineContext.Key<WebSessionPrincipal>
}
