package my.company.app.lib.repository

import my.company.app.db.user.SessionRepository
import my.company.app.db.user.UserRepository
import my.company.app.lib.containerModule

class Repositories(
    val user: UserRepository,
    val session: SessionRepository
) {
    companion object {
        val MODULE = Repositories::class.containerModule()
    }
}
