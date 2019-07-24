package my.company.app.db.repo

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.company.app.initConfig
import my.company.app.lib.TransactionContext
import my.company.app.lib.containerModule
import my.company.app.lib.eager
import my.company.app.lib.ktor.FlywayFeature
import my.company.app.lib.ktor.HikariCPFeature
import my.company.app.lib.repository.Repositories
import my.company.app.test.fixtures.DbFixtures
import org.junit.jupiter.api.Tag
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@Tag("Repository")
abstract class AbstractRepositoryTest {

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
        startKoin {
            modules(listOf(
                containerModule<Repositories>()
            ))
        }
        repo = eager()
        fixtures = DbFixtures()
        try {
            val tx = TransactionContext(hikari.connection)
            try {
                withContext(tx) {
                    testFn()
                }
            } finally {
                hikari.evictConnection(tx.connection)
            }
        } finally {
            stopKoin()
        }
    }
}
