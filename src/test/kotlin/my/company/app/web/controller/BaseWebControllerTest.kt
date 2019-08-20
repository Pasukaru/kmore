package my.company.app.web.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import dev.fixtures.InMemoryFixtures
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import my.company.app.initConfig
import my.company.app.lib.DatabaseService
import my.company.app.lib.koin.KoinCoroutineInterceptor
import my.company.app.lib.koin.eager
import my.company.app.lib.ktor.ParameterParser
import my.company.app.lib.ktor.getKoin
import my.company.app.lib.validation.ValidationService
import my.company.app.mainModule
import my.company.app.test.AbstractTest
import my.company.app.test.declareMock
import my.company.app.web.ErrorResponse
import my.company.app.web.getPathFromLocation
import org.mockito.Mockito
import kotlin.reflect.KClass

abstract class BaseWebControllerTest(
    protected val location: KClass<*>,
    protected val url: String = getPathFromLocation(location)
) : AbstractTest() {

    protected val fixtures = InMemoryFixtures
    protected lateinit var parameterParser: ParameterParser

    protected inline fun TestApplicationEngine.jsonPost(body: Any, crossinline setup: TestApplicationRequest.() -> Unit = {}, testFn: TestApplicationCall.() -> Unit = {}) {
        with(handleRequest(HttpMethod.Post, url) {
            val content = eager<ObjectMapper>().writeValueAsString(body).toByteArray(Charsets.UTF_8)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(content)
            addHeader(HttpHeaders.ContentLength, content.size.toString())
            setup()
        }) {
            testFn()
        }
    }

    protected inline fun TestApplicationEngine.jsonGet(query: String = "", crossinline setup: TestApplicationRequest.() -> Unit = {}, testFn: TestApplicationCall.() -> Unit = {}) {
        val requestUrl = url + query
        with(handleRequest(HttpMethod.Get, requestUrl) {
            setup()
        }) {
            testFn()
        }
    }

    protected open fun Routing.skipInterceptors() {
        // TODO: Find a nicer way than this ugly reflection magic to skip interceptors
        val findPhase = Pipeline::class.java.getDeclaredMethod("findPhase", PipelinePhase::class.java).also { it.isAccessible = true }
        val phase = findPhase(this, ApplicationCallPipeline.Call)
        if (phase != null) {
            val interceptors = phase::class.java.getDeclaredField("interceptors").also { it.isAccessible = true }.get(phase) as ArrayList<*>
            interceptors.clear()
        }
    }

    protected open suspend fun TestApplicationEngine.mockTransactions() {
        val transactionService = declareMock<DatabaseService>()
        Mockito.doAnswer {
            runBlocking {
                @Suppress("UNCHECKED_CAST") val fn = it.arguments.first() as suspend CoroutineScope.() -> Any?
                fn(this)
            }
        }.`when`(transactionService).noTransaction<Any>(any())
        Mockito.doAnswer {
            runBlocking {
                @Suppress("UNCHECKED_CAST") val fn = it.arguments.first() as suspend CoroutineScope.() -> Any?
                fn(this)
            }
        }.`when`(transactionService).transaction<Any>(any())
    }

    protected fun mockValidator(): ValidationService {
        val validator = declareMock<ValidationService>()
        Mockito.doAnswer { it.arguments.first() }.`when`(validator).validate<Any>(any())
        return validator
    }

    protected fun controllerTest(
        profile: String = "test",
        testFn: suspend TestApplicationEngine.() -> Unit
    ) {
        initConfig(profile)
        withTestApplication(Application::mainModule) {
            val koin = this.application.getKoin()
            parameterParser = eager()
            runBlocking(KoinCoroutineInterceptor(koin)) {
                application.routing {
                    skipInterceptors()
                }
                mockTransactions()
                testFn()
            }
        }
    }

    suspend inline fun expectTransaction() {
        Mockito.verify(eager<DatabaseService>(), times(1)).transaction<Any>(any())
    }

    suspend inline fun expectNoTransaction() {
        Mockito.verify(eager<DatabaseService>(), times(1)).noTransaction<Any>(any())
    }

    inline fun <reified RESPONSE_BODY> TestApplicationCall.jsonResponse(): RESPONSE_BODY = response.jsonResponse()
    inline fun <reified RESPONSE_BODY> TestApplicationResponse.jsonResponse(): RESPONSE_BODY {
        return eager<ObjectMapper>().readValue(content, RESPONSE_BODY::class.java)
    }

    fun <RESPONSE_BODY : Any> TestApplicationCall.jsonResponse(responseBody: KClass<RESPONSE_BODY>): RESPONSE_BODY = response.jsonResponse(responseBody)
    fun <RESPONSE_BODY : Any> TestApplicationResponse.jsonResponse(responseBody: KClass<RESPONSE_BODY>): RESPONSE_BODY {
        return eager<ObjectMapper>().readValue(content, responseBody.java)
    }

    inline fun <reified RESPONSE_BODY> TestApplicationCall.jsonResponseList(): List<RESPONSE_BODY> = response.jsonResponseList()
    inline fun <reified RESPONSE_BODY> TestApplicationResponse.jsonResponseList(): List<RESPONSE_BODY> {
        val om = eager<ObjectMapper>()
        val type = om.typeFactory.constructCollectionLikeType(ArrayList::class.java, RESPONSE_BODY::class.java)
        return om.readValue(content, type)
    }

    fun TestApplicationResponse.expectError(status: HttpStatusCode, error: Throwable) {
        assertThat(this.status()).isEqualTo(status)
        val response = jsonResponse<ErrorResponse>()
        assertThat(response).isEqualTo(ErrorResponse(
            errorMessage = error.message!!,
            validationErrors = mutableListOf()
        ))
    }

    inline fun <reified RESPONSE_BODY : Any> TestApplicationCall.expectJsonResponse(status: HttpStatusCode, expectedBody: RESPONSE_BODY): RESPONSE_BODY = expectJsonResponse(RESPONSE_BODY::class, status, expectedBody)

    fun <RESPONSE_BODY : Any> TestApplicationCall.expectJsonResponse(responseBody: KClass<RESPONSE_BODY>, status: HttpStatusCode, expectedBody: RESPONSE_BODY): RESPONSE_BODY {
        assertThat(this.response.status()).isEqualTo(status)
        val response = jsonResponse(responseBody)
        assertThat(response).isEqualTo(expectedBody)
        return response
    }

    inline fun <reified RESPONSE_BODY : Any> TestApplicationCall.expectJsonResponseList(status: HttpStatusCode, expectedBody: List<RESPONSE_BODY>): List<RESPONSE_BODY> {
        assertThat(this.response.status()).isEqualTo(status)
        val response = jsonResponseList<RESPONSE_BODY>()
        assertThat(response).isEqualTo(expectedBody)
        return response
    }

    fun ValidationService.hasValidated(obj: Any) {
        Mockito.verify(this, Mockito.times(1)).validate(obj)
    }
}
