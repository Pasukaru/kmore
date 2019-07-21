package my.company.app.web.controller.session

import my.company.app.business_logic.session.LoginRequest
import my.company.app.business_logic.session.LoginResponse

class WebSessionMapper {

    fun req(request: WebLoginRequest) = LoginRequest(
        email = request.email,
        passwordClean = request.password
    )

    fun res(response: LoginResponse) = WebLoginResponse(
        id = response.id
    )
}
