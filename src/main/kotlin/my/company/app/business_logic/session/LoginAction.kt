package my.company.app.business_logic.session

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.db.newSession
import java.util.UUID

data class LoginRequest(val email: String, val passwordClean: String)
data class LoginResponse(val id: UUID)

class LoginAction : BusinessLogicAction<LoginRequest, LoginResponse>() {
    override suspend fun execute(request: LoginRequest): LoginResponse {
        val user = repo.user.findByEmailIgnoringCase(request.email) ?: throw InvalidLoginCredentialsException()
        if (user.password != request.passwordClean) throw InvalidLoginCredentialsException()

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
