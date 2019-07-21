package my.company.app.web.controller.session

import my.company.app.business_logic.session.LoginRequest
import my.company.jooq.tables.records.SessionRecord

class WebSessionMapper {

    fun req(request: WebLoginRequest) = LoginRequest(
        email = request.email,
        passwordClean = request.password
    )

    fun res(response: SessionRecord) = WebLoginResponse(
        id = response.id
    )
}
