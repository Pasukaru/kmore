package my.company.app.db.repo

import dev.fixtures.DbFixtures
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.company.app.business_logic.TransactionOptions
import my.company.app.initConfig
import my.company.app.lib.TimeService
import my.company.app.lib.TransactionContext
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.containerModule
import my.company.app.lib.koin.eager
import my.company.app.lib.ktor.FlywayFeature
import my.company.app.lib.ktor.HikariCPFeature
import my.company.app.lib.repository.Repositories
import my.company.app.test.AbstractTest
import my.company.app.test.MockedTimeService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.parallel.ResourceLock
import org.koin.core.KoinApplication
import org.koin.dsl.module

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
    lateinit var mockedTimeService: TimeService

    protected fun resetDatabase() {
        val con = hikari.connection
        con.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE").execute()
        flyway.migrate()
        hikari.evictConnection(con)
    }

    protected lateinit var koin: KoinApplication

    @BeforeEach
    protected fun beforeEach() {
        koin = KoinContext.startKoin {
            modules(listOf(
                module { single { MockedTimeService.mock } },
                containerModule<Repositories>()
            ))
        }
        repo = eager()
        fixtures = DbFixtures()
        mockedTimeService = eager()
        resetDatabase()
    }

    @AfterEach
    protected fun afterEach() {
        KoinContext.stopKoin()
    }

    protected fun queryTest(testFn: suspend () -> Unit) = runBlocking {
        val connection = hikari.connection
        try {
            withContext(KoinContext.asContextElement(koin)) {
                TransactionContext(connection).transaction(TransactionOptions.DEFAULT.isolationLevel, readOnly = false) {
                    testFn()
                }
            }
        } finally {
            hikari.evictConnection(connection)
        }
    }
}
