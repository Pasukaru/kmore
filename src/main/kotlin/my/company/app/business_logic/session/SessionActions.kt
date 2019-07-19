package my.company.app.business_logic.session

import my.company.app.lib.containerModule

class SessionActions(
    val login: LoginAction
) {
    companion object {
        val MODULE = SessionActions::class.containerModule()
    }
}
