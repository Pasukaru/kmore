package my.company.app.business_logic.user

import my.company.app.business_logic.user.dto.UpdateUserByIdAction
import my.company.app.business_logic.user.dto.UpdateUserByIdResult
import my.company.app.lib.service.AbstractService
import my.company.app.log

class UserService : AbstractService() {
    suspend fun updateUserById(action: UpdateUserByIdAction): UpdateUserByIdResult {
        val user = repo.user.findById(action.id).await()
        log("Found User: $user")
        return UpdateUserByIdResult(
            id = user.id,
            name = action.name + user.name
        )
    }
}