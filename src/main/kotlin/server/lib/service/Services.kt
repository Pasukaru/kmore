@file:Suppress("EXPERIMENTAL_API_USAGE")

package server.lib.service

import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import server.KtorMain
import server.user.UserService

class Services : KodeinAware {
    override val kodein by lazy { KtorMain.kodein }
    val user: UserService by instance()
}
