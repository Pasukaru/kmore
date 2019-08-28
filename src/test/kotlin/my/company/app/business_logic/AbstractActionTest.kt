package my.company.app.business_logic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import dev.fixtures.InMemoryFixtures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.runBlocking
import my.company.app.db.IsolationLevel
import my.company.app.db.ModelGenerator
import my.company.app.initConfig
import my.company.app.lib.AuthorizationService
import my.company.app.lib.DatabaseService
import my.company.app.lib.IdGenerator
import my.company.app.lib.TimeService
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.eager
import my.company.app.lib.repository.Repositories
import my.company.app.lib.validation.ValidationService
import my.company.app.test.AbstractTest
import my.company.app.test.MockedTimeService
import my.company.app.test.captor
import my.company.app.test.mockedContainerModule
import my.company.app.test.singleValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.mockito.Mockito

abstract class AbstractActionTest : AbstractTest() {

    open val profile: String = "test"

    lateinit var koin: KoinApplication
    lateinit var repo: Repositories
    lateinit var mockedTimeService: TimeService
    lateinit var validationService: ValidationService
    lateinit var spiedModelGenerator: ModelGenerator
    lateinit var databaseService: DatabaseService

    lateinit var mockedAuthorizationService: AuthorizationService

    protected val fixtures = InMemoryFixtures

    protected val transactionIsolationLevelCaptor = captor<IsolationLevel>()
    protected val transactionReadOnlyCaptor = captor<Boolean>()

    @BeforeEach
    protected open fun beforeEach() {
        koin = KoinContext.startKoin {
            modules(listOf(
                module { single { initConfig(profile) } },
                module { single { IdGenerator() } },
                module { single { Mockito.mock(ValidationService::class.java) } },
                module { single { MockedTimeService.mock } },
                module { single { Mockito.spy(ModelGenerator()) } },
                module { single { Mockito.mock(AuthorizationService::class.java) } },
                module { single { Mockito.mock(DatabaseService::class.java) } },
                mockedContainerModule<Repositories>()
            ))
        }
        validationService = eager()
        mockedTimeService = eager()
        spiedModelGenerator = eager()
        mockedAuthorizationService = eager()
        databaseService = eager()
        repo = eager()
        Mockito.doAnswer { it.arguments.first() }.`when`(validationService).validate<Any>(any())
        mockTransactions()
    }

    @AfterEach
    protected open fun afterEach() {
        KoinContext.stopKoin()
    }

    protected open fun mockTransactions() = runBlocking {
        Mockito.doAnswer {
            runBlocking {
                @Suppress("UNCHECKED_CAST") val fn = it.arguments.last() as suspend CoroutineScope.() -> Any?
                fn(this)
            }
        }.`when`(databaseService).transaction<Any>(capture(transactionIsolationLevelCaptor), capture(transactionReadOnlyCaptor), any(), any())
    }

    fun expectTransaction(isolationLevel: IsolationLevel = IsolationLevel.READ_COMMITTED, readOnly: Boolean = false) {
        assertThat(transactionIsolationLevelCaptor.singleValue).isEqualTo(isolationLevel)
        assertThat(transactionReadOnlyCaptor.singleValue).isEqualTo(readOnly)
    }

    fun actionTest(testFn: suspend () -> Unit) {
        runBlocking(KoinContext.asContextElement(koin)) { testFn() }
    }
}
