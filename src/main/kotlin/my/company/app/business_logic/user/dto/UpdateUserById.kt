package my.company.app.business_logic.user.dto

import java.util.*

data class UpdateUserByIdAction(val id: UUID, val name: String)
data class UpdateUserByIdResult(val id: UUID, val name: String)