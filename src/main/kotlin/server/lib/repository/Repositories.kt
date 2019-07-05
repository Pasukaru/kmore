package server.lib.repository

import org.kodein.di.generic.instance
import server.user.UserRepository

class Repositories : AbstractRepository() {
    val user: UserRepository by instance()
}