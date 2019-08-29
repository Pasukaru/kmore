package my.company.app.business_logic.session

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.generated.jooq.tables.records.SessionRecord
import my.company.app.lib.InvalidLoginCredentialsException
import my.company.app.lib.PasswordHelper
import my.company.app.lib.koin.lazy

data class LoginRequest(val email: String, val passwordClean: String)

class LoginAction : BusinessLogicAction<LoginRequest, SessionRecord>() {
    private val passwordHelper: PasswordHelper by lazy()

    override suspend fun action(request: LoginRequest): SessionRecord {
        val user = repo.user.findByEmailIgnoringCase(request.email) ?: throw InvalidLoginCredentialsException()
        if (!passwordHelper.checkPassword(request.passwordClean, user.password)) throw InvalidLoginCredentialsException()

        return repo.session.insert(generate.session(
            userId = user.id
        ))
    }
}
