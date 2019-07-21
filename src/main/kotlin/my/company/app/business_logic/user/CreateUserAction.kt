package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.db.newUser
import my.company.app.lib.PasswordHelper
import my.company.app.lib.UserByEmailAlreadyExistsException
import my.company.app.lib.inject
import my.company.app.lib.validation.Email
import my.company.app.lib.validation.NotBlank
import my.company.app.lib.validation.Password
import my.company.jooq.tables.records.UserRecord

data class CreateUserRequest(
    @field:Email
    val email: String,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    @Password
    val passwordClean: String
)

class CreateUserAction : BusinessLogicAction<CreateUserRequest, UserRecord>() {
    private val passwordHelper: PasswordHelper by inject()

    override suspend fun action(request: CreateUserRequest): UserRecord {
        validator.validate(request)
        if (repo.user.existsByEmailIgnoringCase(request.email)) throw UserByEmailAlreadyExistsException()

        return repo.user.insert(newUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = passwordHelper.hashPassword(request.passwordClean)
        ))
    }
}
