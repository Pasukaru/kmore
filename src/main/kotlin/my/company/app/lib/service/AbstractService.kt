package my.company.app.lib.service

import my.company.app.inject
import my.company.app.lib.repository.Repositories

abstract class AbstractService : Service {
    protected val repo: Repositories by inject()
    protected val svc: Services by inject()
}