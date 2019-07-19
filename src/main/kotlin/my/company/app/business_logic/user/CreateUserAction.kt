package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.db.newUser
import java.util.UUID

data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val passwordClean: String
)

data class CreateUserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
)

class CreateUserAction : BusinessLogicAction<CreateUserRequest, CreateUserResponse>() {
    override suspend fun execute(request: CreateUserRequest): CreateUserResponse {
        repo.user.findByEmailIgnoringCase(request.email)?.also {
            repo.user.deleteById(it.id)
        }

        val user = newUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = request.passwordClean // TODO: Hashing
        ).let { repo.user.insert(it) }

        return CreateUserResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }
}
