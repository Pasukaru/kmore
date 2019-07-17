package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.app.db.user.newUser
import java.util.*


class CreateUserAction : BusinessLogicAction<CreateUserAction.Request, CreateUserAction.Response>() {

    data class Request(val email: String, val firstName: String, val lastName: String, val passwordClean: String)
    data class Response(val id: UUID, val email: String, val firstName: String, val lastName: String)

    override suspend fun execute(request: Request): Response {

        repo.user.findByEmailIgnoringCase(request.email)?.also {
            repo.user.deleteById(it.id)
        }

        val user = newUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = request.passwordClean // TODO: Hashing
        ).let { repo.user.insert(it) }

        return Response(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }
}
