package my.company.app.web.controller.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.user.CreateUserAction
import my.company.app.business_logic.user.CreateUserRequest
import my.company.app.db.newUser
import my.company.app.lib.validation.ValidationService
import my.company.app.web.controller.BaseWebControllerTest
import org.junit.Test
import org.mockito.Mockito

class WebCreateUserTest : BaseWebControllerTest(WebCreateUserLocation::class) {

    @Test
    fun requestIsValidated() {
        expectEmailValidation(WebCreateUserRequest::email)
        expectNotBlankValidation(WebCreateUserRequest::firstName)
        expectNotBlankValidation(WebCreateUserRequest::lastName)
        expectPasswordValidation(WebCreateUserRequest::password)
    }

    @Test
    fun returnsCorrectResult() = controllerTest {
        val actionMock = declareMock<CreateUserAction>()
        val validator = declareSpy<ValidationService>()

        val request = WebCreateUserRequest(
            email = "email@derp.com",
            firstName = "firstName",
            lastName = "lastName",
            password = "passwordAD12345!!!"
        )
        val actionRequest = captor<CreateUserRequest>()
        val mockedResponse = newUser(
            email = "random@email.com",
            firstName = "blubb",
            lastName = "bla",
            password = "123"

        )
        Mockito.doReturn(mockedResponse).`when`(actionMock).execute(actionRequest.capture())

        jsonPost(request) {
            expectJsonResponse(HttpStatusCode.Created, WebCreateUserResponse(
                id = mockedResponse.id,
                email = mockedResponse.email,
                firstName = mockedResponse.firstName,
                lastName = mockedResponse.lastName
            ))
            assertThat(actionRequest.value).isEqualTo(CreateUserRequest(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                passwordClean = request.password
            ))
        }

        validator.hasValidated(request)
    }
}
