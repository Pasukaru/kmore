package my.company.app.db.repo

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.company.app.initConfig
import my.company.app.lib.TransactionContext
import my.company.app.lib.containerModule
import my.company.app.lib.di.KoinContext
import my.company.app.lib.di.KoinCoroutineInterceptor
import my.company.app.lib.eager
import my.company.app.lib.ktor.FlywayFeature
import my.company.app.lib.ktor.HikariCPFeature
import my.company.app.lib.repository.Repositories
import my.company.app.test.AbstractTest
import my.company.app.test.fixtures.DbFixtures
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Tag("Repository")
@Execution(ExecutionMode.SAME_THREAD)
abstract class AbstractRepositoryTest : AbstractTest() {

    companion object {
        protected val config by lazy { initConfig("test") }
        protected val flyway by lazy { FlywayFeature.flyway(config) }
        protected val hikari by lazy { HikariCPFeature.hikari((config)) }
    }

    lateinit var repo: Repositories
    lateinit var fixtures: DbFixtures

    protected fun resetDatabase() {
        val con = hikari.connection
        con.prepareStatement("DROP SCHEMA public CASCADE").execute()
        flyway.migrate()
        hikari.evictConnection(con)
    }

    protected fun queryTest(testFn: suspend () -> Unit) = runBlocking {
        resetDatabase()
        val koin = KoinContext.startKoin {
            modules(listOf(
                containerModule<Repositories>()
            ))
        }
        repo = eager()
        fixtures = DbFixtures()
        try {
            val tx = TransactionContext(hikari.connection)
            try {
                withContext(KoinCoroutineInterceptor(koin) + tx) {
                    testFn()
                }
            } finally {
                hikari.evictConnection(tx.connection)
            }
        } finally {
            KoinContext.stopKoin()
        }
    }
}
