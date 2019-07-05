@file:Suppress("EXPERIMENTAL_API_USAGE")

package server.lib.service

import server.containerModule
import server.user.UserService

class Services(
    val user: UserService
) {
    companion object {
        val MODULE = Services::class.containerModule()
    }
}