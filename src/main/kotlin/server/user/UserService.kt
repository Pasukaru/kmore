package server.user

import log
import server.lib.service.AbstractService
import server.user.dto.UpdateUserByIdRequest
import server.user.dto.UpdateUserByIdResponse
import java.util.*

class UserService : AbstractService() {
    suspend fun getUserById(id: UUID, request: UpdateUserByIdRequest): UpdateUserByIdResponse {
        val user = repo.user.findById(id).await()
        log("Found User: $user")
        return UpdateUserByIdResponse(id = user.id, name = request.name + user.name)
    }
}