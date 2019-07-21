package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.jooq.tables.records.UserRecord

class GetUsersAction : BusinessLogicAction<Unit, List<UserRecord>>() {
    override suspend fun action(request: Unit): List<UserRecord> {
        authorizationService.expectPermission("USERS_CAN_READ")
        return repo.user.findAll()
    }
}
