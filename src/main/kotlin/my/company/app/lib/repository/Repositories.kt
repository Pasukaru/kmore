package my.company.app.lib.repository

import my.company.app.containerModule
import my.company.app.db.user.UserRepository

class Repositories(
    val user: UserRepository
) {
    companion object {
        val MODULE = Repositories::class.containerModule()
    }
}
