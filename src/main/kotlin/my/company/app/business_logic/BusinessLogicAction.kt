package my.company.app.business_logic

import my.company.app.inject
import my.company.app.lib.repository.Repositories

abstract class BusinessLogicAction<REQUEST, RESPONSE> {
    protected val repo: Repositories by inject()

    abstract suspend fun execute(request: REQUEST): RESPONSE
}
