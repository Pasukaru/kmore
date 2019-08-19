package my.company.app.business_logic.user

import my.company.app.business_logic.BusinessLogicAction
import my.company.jooq.tables.records.UserRecord
import java.time.Instant

data class GetUsersFilter(
    val email: String?,
    val name: String?,
    val createdAtBefore: Instant?
)

class GetUsersAction : BusinessLogicAction<GetUsersFilter, List<UserRecord>>() {
    override suspend fun action(request: GetUsersFilter): List<UserRecord> {
        authorizationService.expectPermission("USERS_CAN_READ")
        return repo.user.findByFilter(request)
    }
}
