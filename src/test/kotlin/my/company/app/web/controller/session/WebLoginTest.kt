package my.company.app.web.controller.session

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.session.LoginAction
import my.company.app.business_logic.session.LoginRequest
import my.company.app.lib.InvalidLoginCredentialsException
import my.company.app.lib.validation.ValidationService
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.declareSpy
import my.company.app.test.singleValue
import my.company.app.web.GlobalWebErrorHandler
import my.company.app.web.controller.BaseWebControllerTest
import my.company.jooq.tables.records.SessionRecord
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import java.util.UUID

class WebLoginTest : BaseWebControllerTest(WebLoginLocation::class) {

    @Test
    fun returnsCorrectResult() = controllerTest {
        val actionMock = declareMock<LoginAction>()
        val validator = declareSpy<ValidationService>()

        val request = WebLoginRequest(email = "email", password = "password")
        val actionRequest = captor<LoginRequest>()
        val mockedResponse = SessionRecord().also { it.id = UUID.randomUUID() }
        Mockito.doReturn(mockedResponse).`when`(actionMock).execute(capture(actionRequest))

        jsonPost(request) {
            expectTransaction()
            expectJsonResponse(HttpStatusCode.Created, WebLoginResponse(id = mockedResponse.id))
            assertThat(actionRequest.singleValue).isEqualTo(LoginRequest(email = request.email, passwordClean = request.password))
        }

        validator.hasValidated(request)
    }

    @Test
    fun returnsCallsGlobalErrorHandlerOnError() = controllerTest {
        val actionMock = declareMock<LoginAction>()
        val errorHandler = declareSpy<GlobalWebErrorHandler>()
        val error = InvalidLoginCredentialsException()

        Mockito.doThrow(error).`when`(actionMock).execute(any())

        jsonPost(WebLoginRequest(email = "email", password = "password")) {
            Mockito.verify(errorHandler, times(1)).handleError(any(), any())
            response.expectError(HttpStatusCode.Unauthorized, InvalidLoginCredentialsException())
        }
    }
}
