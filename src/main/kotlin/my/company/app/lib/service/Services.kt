@file:Suppress("EXPERIMENTAL_API_USAGE")

package my.company.app.lib.service

import my.company.app.business_logic.user.UserService
import my.company.app.containerModule

class Services(
    val user: UserService
) {
    companion object {
        val MODULE = Services::class.containerModule()
    }
}