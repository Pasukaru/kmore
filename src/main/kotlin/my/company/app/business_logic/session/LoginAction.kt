package my.company.app.business_logic.session

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.db.newSession
import my.company.app.lib.PasswordHelper
import my.company.app.lib.inject
import java.util.UUID

data class LoginRequest(val email: String, val passwordClean: String)
data class LoginResponse(val id: UUID)

class LoginAction : BusinessLogicAction<LoginRequest, LoginResponse>() {
    private val passwordHelper: PasswordHelper by inject()

    override suspend fun action(request: LoginRequest): LoginResponse {
        val user = repo.user.findByEmailIgnoringCase(request.email) ?: throw InvalidLoginCredentialsException()
        if (!passwordHelper.checkPassword(request.passwordClean, user.password)) throw InvalidLoginCredentialsException()

        val session = repo.session.insert(
            newSession(
                userId = user.id
            )
        )

        return LoginResponse(
            id = session.id
        )
    }
}
