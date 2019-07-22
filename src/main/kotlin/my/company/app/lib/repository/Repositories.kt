package my.company.app.lib.repository

import my.company.app.db.user.SessionRepository
import my.company.app.db.user.UserRepository

class Repositories(
    val user: UserRepository,
    val session: SessionRepository
)
