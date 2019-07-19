package my.company.app.lib.ktor

import com.zaxxer.hikari.pool.HikariPool
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import my.company.app.business_logic.user.CreateUserRequest
import my.company.app.business_logic.user.UserActions
import my.company.app.lib.eager
import my.company.app.lib.logger
import my.company.app.lib.repository.Repositories
import my.company.app.lib.tx.transaction
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

object ApplicationWarmup : ApplicationFeature<Application, Unit, Unit> {
    private val logger = logger<ApplicationWarmup>()

    override val key: AttributeKey<Unit>
        get() = AttributeKey(this::class.java.canonicalName)

    @Suppress("MagicNumber")
    override fun install(pipeline: Application, configure: (Unit).() -> Unit) {
        val time = measureTimeMillis {
            runBlocking {
                val hikari = eager<HikariPool>()
                (0 until 10)
                    .map { it }
                    .parallelStream()
                    .map { hikari.connection }
                    .toList()
                    .map { hikari.evictConnection(it) }

                val repositories = eager<Repositories>()
                val userActions = eager<UserActions>()

                transaction {
                    userActions.createUser.execute(CreateUserRequest("", "", "", ""))
                    repositories.user.findByEmailIgnoringCase("asdf")
                }
            }
        }
        logger.debug("Warm up took: ${time}ms")
    }
}
