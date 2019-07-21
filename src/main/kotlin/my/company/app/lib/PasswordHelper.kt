package my.company.app.lib

import org.springframework.security.crypto.bcrypt.BCrypt
import java.security.SecureRandom

class PasswordHelper {

    companion object {
        private const val GENSALT_DEFAULT_LOG2_ROUNDS = 10
        private val SECURE_RANDOM = SecureRandom()
    }

    fun hashPassword(passwordClean: String): String {
        return BCrypt.hashpw(passwordClean, BCrypt.gensalt(GENSALT_DEFAULT_LOG2_ROUNDS, SECURE_RANDOM))
    }

    fun checkPassword(passwordClean: String, passwordHash: String): Boolean =
        tryCatch({ BCrypt.checkpw(passwordClean, passwordHash) }, { false })
}
