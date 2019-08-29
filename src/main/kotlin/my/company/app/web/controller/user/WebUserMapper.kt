package my.company.app.web.controller.user

import my.company.app.business_logic.user.CreateUserRequest
import my.company.app.business_logic.user.GetUsersFilter
import my.company.app.generated.jooq.tables.records.UserRecord

class WebUserMapper {

    fun req(request: WebCreateUserRequest) = CreateUserRequest(
        email = request.email,
        firstName = request.firstName,
        lastName = request.lastName,
        passwordClean = request.password
    )

    fun res(response: UserRecord) = WebCreateUserResponse(
        id = response.id,
        email = response.email,
        firstName = response.firstName,
        lastName = response.lastName
    )

    fun req(query: WebGetUsersRequestQuery) = GetUsersFilter(
        email = query.email,
        name = query.name,
        createdAtBefore = query.createdAtBefore
    )

    fun res(response: List<UserRecord>) = response.map { user ->
        WebGetUsersResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName
        )
    }
}
