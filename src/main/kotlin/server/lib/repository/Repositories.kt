package server.lib.repository

import server.containerModule
import server.user.UserRepository

class Repositories(
    val user: UserRepository
) : AbstractRepository() {
    companion object {
        val MODULE = Repositories::class.containerModule()
    }
}