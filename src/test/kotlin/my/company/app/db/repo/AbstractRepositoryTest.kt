package my.company.app.db.repo

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.company.app.initConfig
import my.company.app.lib.TransactionContext
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.KoinCoroutineInterceptor
import my.company.app.lib.koin.containerModule
import my.company.app.lib.koin.eager
import my.company.app.lib.ktor.FlywayFeature
import my.company.app.lib.ktor.HikariCPFeature
import my.company.app.lib.repository.Repositories
import my.company.app.test.AbstractTest
import my.company.app.test.fixtures.DbFixtures
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.parallel.ResourceLock

@Tag("Repository")
@ResourceLock("Database")
abstract class AbstractRepositoryTest : AbstractTest() {

    companion object {
        protected val config by lazy { initConfig("test") }
        // A bit dirty, these resources are never closed.
        // We rely on the JVM to close them once all tests are completed and the JVM shuts down.
        // Haven't found a way in Junit 5 to run something after ALL tests have been executed.
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

    protected fun queryTest(testFn: suspend () -> Unit) {
        resetDatabase()
        val koin = KoinContext.startKoin {
            modules(listOf(
                containerModule<Repositories>()
            ))
        }
        repo = eager()
        fixtures = DbFixtures()
        try {
            runBlocking {
                val tx = TransactionContext(hikari.connection)
                try {
                    withContext(KoinCoroutineInterceptor(koin) + tx) {
                        testFn()
                    }
                } finally {
                    hikari.evictConnection(tx.connection)
                }
            }
        } finally {
            KoinContext.stopKoin()
        }
    }
}
