package my.company.app.business_logic

import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.runBlocking
import my.company.app.initConfig
import my.company.app.lib.eager
import my.company.app.lib.repository.Repositories
import my.company.app.lib.validation.ValidationService
import my.company.app.test.mockedContainerModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito

abstract class AbstractActionTest {

    open val profile: String = "test"

    lateinit var repo: Repositories
    lateinit var validationService: ValidationService

    @BeforeEach
    open fun beforeEach() {
        startKoin {
            modules(listOf(
                module { single { initConfig(profile) } },
                module { single { Mockito.mock(ValidationService::class.java) } },
                mockedContainerModule<Repositories>()
            ))
        }
        repo = eager()
        validationService = eager()
        Mockito.doAnswer { it.arguments.first() }.`when`(validationService).validate<Any>(any())
    }

    fun actionTest(testFn: suspend () -> Unit) = runBlocking {
        testFn()
    }

    @AfterEach
    open fun afterEach() {
        stopKoin()
    }
}
