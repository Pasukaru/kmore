package my.company.app.web.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import my.company.app.initConfig
import my.company.app.lib.eager
import my.company.app.lib.validation.Email
import my.company.app.lib.validation.NotBlank
import my.company.app.lib.validation.Password
import my.company.app.lib.validation.ValidationService
import my.company.app.mainModule
import my.company.app.test.KotlinArgumentCaptor
import my.company.app.web.ErrorResponse
import my.company.app.web.getPathFromLocation
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.mockito.Mockito
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class BaseWebControllerTest(
    protected val location: KClass<*>,
    protected val url: String = getPathFromLocation(location)
) {

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

    protected inline fun TestApplicationEngine.jsonGet(crossinline setup: TestApplicationRequest.() -> Unit = {}, testFn: TestApplicationCall.() -> Unit = {}) {
        with(handleRequest(HttpMethod.Get, url) {
            setup()
        }) {
            testFn()
        }
    }

    open fun controllerTest(profile: String = "test", testFn: suspend TestApplicationEngine.() -> Unit) {
        initConfig(profile)
        withTestApplication(Application::mainModule) {
            runBlocking {
                testFn()
            }
        }
    }

    inline fun <reified BEAN : Any> TestApplicationEngine.declareSpy(qualifier: Qualifier? = null): BEAN {
        val bean = eager(BEAN::class, qualifier)
        val spy = Mockito.spy(bean)!!
        val koin = GlobalContext.get()
        koin.modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { spy } })
        return spy
    }

    inline fun <reified BEAN : Any> TestApplicationEngine.declareMock(qualifier: Qualifier? = null): BEAN {
        val spy = Mockito.mock(BEAN::class.java)!!
        val koin = GlobalContext.get()
        koin.modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { spy } })
        return spy
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

    inline fun <reified TYPE : Any> captor(): KotlinArgumentCaptor<TYPE> = KotlinArgumentCaptor.forClass()

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

    @Suppress("MemberVisibilityCanBePrivate")
    fun expectNotNullProperty(property: KProperty<*>) {
        assertThat(property.returnType.isMarkedNullable, "Property $property is not optional and should not be nullable").isFalse()
    }

    fun expectEmailValidation(property: KProperty<String>, optional: Boolean = false) {
        val annotation = property.javaField?.getAnnotation(Email::class.java)
        assertThat(annotation, "Expected @Password annotation on $property").isNotNull()
        if (!optional) expectNotNullProperty(property)
    }

    fun expectNotBlankValidation(property: KProperty<String>, optional: Boolean = false) {
        val annotation = property.javaField?.getAnnotation(NotBlank::class.java)
        assertThat(annotation, "Expected @NotBlank annotation on $property").isNotNull()
        if (!optional) expectNotNullProperty(property)
    }

    fun expectPasswordValidation(property: KProperty<String>, optional: Boolean = false) {
        val annotation = property.javaField?.getAnnotation(Password::class.java)
        assertThat(annotation, "Expected @Email annotation on $property").isNotNull()
        if (!optional) expectNotNullProperty(property)
    }

    fun ValidationService.hasValidated(obj: Any) {
        Mockito.verify(this, Mockito.times(1)).validate(obj)
    }
}
