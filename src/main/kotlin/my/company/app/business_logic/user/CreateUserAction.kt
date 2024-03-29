package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.generated.jooq.tables.records.UserRecord
import my.company.app.lib.PasswordHelper
import my.company.app.lib.UserByEmailAlreadyExistsException
import my.company.app.lib.koin.lazy
import my.company.app.lib.validation.Email
import my.company.app.lib.validation.NotBlank
import my.company.app.lib.validation.Password

data class CreateUserRequest(
    @field:Email
    val email: String,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    @field:Password
    val passwordClean: String
)

class CreateUserAction : BusinessLogicAction<CreateUserRequest, UserRecord>() {
    private val passwordHelper: PasswordHelper by lazy()

    override suspend fun action(request: CreateUserRequest): UserRecord {
        if (repo.user.existsByEmailIgnoringCase(request.email)) throw UserByEmailAlreadyExistsException()

        return repo.user.insert(generate.user(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = passwordHelper.hashPassword(request.passwordClean)
        ))
    }
}
