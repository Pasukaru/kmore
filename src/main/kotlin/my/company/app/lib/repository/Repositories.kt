package my.company.app.lib.repository

import my.company.app.db.repo.SessionRepository
import my.company.app.db.repo.UserRepository

class Repositories(
    val user: UserRepository,
    val session: SessionRepository
)
