package my.company.app.web.controller.user

import my.company.app.business_logic.user.CreateUserRequest
import my.company.jooq.tables.records.UserRecord

class WebUserMapper {

    fun req(request: WebCreateUserRequest) = CreateUserRequest(
        email = request.email,
        firstName = request.firstName,
        lastName = request.lastName,
        passwordClean = request.password
    )

    fun res(response: UserRecord) = WebCreateUserResponse(
        id = response.id,
        email = response.lastName,
        firstName = response.firstName,
        lastName = response.lastName
    )

    fun res(response: List<UserRecord>) = response.map { user ->
        WebGetUsersResponse(
            id = user.id,
            email = user.lastName,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }
}
