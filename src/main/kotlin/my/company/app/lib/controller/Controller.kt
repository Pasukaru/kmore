package my.company.app.lib.controller

import io.ktor.routing.Routing

interface Controller {
    val routing: Routing.() -> Unit
}