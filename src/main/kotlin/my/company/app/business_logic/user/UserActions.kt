package my.company.app.business_logic.user

import my.company.app.lib.containerModule

class UserActions(
    val createUser: CreateUserAction,
    val getUsers: GetUsersAction
) {
    companion object {
        val MODULE = UserActions::class.containerModule()
    }
}
