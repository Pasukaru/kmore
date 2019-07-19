package my.company.app.lib.ktor.web.controller.user

import my.company.app.business_logic.user.CreateUserRequest
import my.company.app.business_logic.user.CreateUserResponse

class WebUserMapper {

    fun req(request: WebCreateUserRequest) = CreateUserRequest(
        email = request.email,
        firstName = request.firstName,
        lastName = request.lastName,
        passwordClean = request.password
    )

    fun res(response: CreateUserResponse) = WebCreateUserResponse(
        id = response.id,
        email = response.lastName,
        firstName = response.firstName,
        lastName = response.lastName
    )
}
