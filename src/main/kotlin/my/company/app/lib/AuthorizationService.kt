package my.company.app.lib

import my.company.app.web.auth.WebSessionPrincipal
import kotlin.coroutines.coroutineContext

class AuthorizationService {
    suspend fun expectPermission(permission: String) {
        val webAuth = coroutineContext[WebSessionPrincipal]
        if (webAuth != null) {
            if (webAuth.user.permissions.contains(permission)) return
        }
        throw InsufficientPermissionsException()
    }
}
