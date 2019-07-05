package server.lib.service

import server.inject
import server.lib.repository.Repositories

abstract class AbstractService : Service {
    val repo: Repositories by inject()
    val svc: Services by inject()
}