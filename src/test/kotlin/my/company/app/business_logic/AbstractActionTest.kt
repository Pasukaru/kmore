package my.company.app.business_logic

import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.runBlocking
import my.company.app.MockedTimeService
import my.company.app.db.ModelGenerator
import my.company.app.initConfig
import my.company.app.lib.AuthorizationService
import my.company.app.lib.IdGenerator
import my.company.app.lib.TimeService
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.KoinCoroutineInterceptor
import my.company.app.lib.koin.eager
import my.company.app.lib.repository.Repositories
import my.company.app.lib.validation.ValidationService
import my.company.app.test.AbstractTest
import my.company.app.test.fixtures.InMemoryFixtures
import my.company.app.test.mockedContainerModule
import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.mockito.Mockito

abstract class AbstractActionTest : AbstractTest() {

    open val profile: String = "test"

    lateinit var repo: Repositories
    lateinit var mockedTimeService: TimeService
    lateinit var validationService: ValidationService
    lateinit var spiedModelGenerator: ModelGenerator
    lateinit var mockedAuthorizationService: AuthorizationService

    protected val fixtures = InMemoryFixtures

    open fun beforeEach(): KoinApplication {
        val koin = KoinContext.startKoin {
            modules(listOf(
                module { single { initConfig(profile) } },
                module { single { IdGenerator() } },
                module { single { Mockito.mock(ValidationService::class.java) } },
                module { single { MockedTimeService.mock } },
                module { single { Mockito.spy(ModelGenerator()) } },
                module { single { Mockito.mock(AuthorizationService::class.java) } },
                mockedContainerModule<Repositories>()
            ))
        }
        validationService = eager()
        mockedTimeService = eager()
        spiedModelGenerator = eager()
        mockedAuthorizationService = eager()
        repo = eager()
        Mockito.doAnswer { it.arguments.first() }.`when`(validationService).validate<Any>(any())
        return koin
    }

    fun actionTest(testFn: suspend () -> Unit) {
        val koin = beforeEach()
        try {
            runBlocking(KoinCoroutineInterceptor(koin)) { testFn() }
        } finally {
            afterEach(koin)
        }
    }

    open fun afterEach(koin: KoinApplication) {
        koin.close()
    }
}
