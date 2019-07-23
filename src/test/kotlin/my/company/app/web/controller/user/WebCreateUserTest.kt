package my.company.app.web.controller.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.capture
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.user.CreateUserAction
import my.company.app.business_logic.user.CreateUserRequest
import my.company.app.lib.Faker
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.expectEmailValidation
import my.company.app.test.expectNotBlankValidation
import my.company.app.test.expectPasswordValidation
import my.company.app.test.singleValue
import my.company.app.web.controller.BaseWebControllerTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito

class WebCreateUserTest : BaseWebControllerTest(WebCreateUserLocation::class) {

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun requestIsValidated() {
        expectEmailValidation(WebCreateUserRequest::email)
        expectNotBlankValidation(WebCreateUserRequest::firstName)
        expectNotBlankValidation(WebCreateUserRequest::lastName)
        expectPasswordValidation(WebCreateUserRequest::password)
    }

    @Test
    fun returnsCorrectResult() = controllerTest {
        val actionMock = declareMock<CreateUserAction>()
        val validator = mockValidator()

        val request = WebCreateUserRequest(
            email = Faker.internet().emailAddress(),
            firstName = Faker.name().firstName(),
            lastName = Faker.name().lastName(),
            password = Faker.internet().password()
        )
        val actionRequest = captor<CreateUserRequest>()
        val mockedResponse = fixtures.user()
        Mockito.doReturn(mockedResponse).`when`(actionMock).execute(capture(actionRequest))

        jsonPost(request) {
            expectJsonResponse(HttpStatusCode.Created, WebCreateUserResponse(
                id = mockedResponse.id,
                email = mockedResponse.email,
                firstName = mockedResponse.firstName,
                lastName = mockedResponse.lastName
            ))
            assertThat(actionRequest.singleValue).isEqualTo(CreateUserRequest(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                passwordClean = request.password
            ))
        }
        validator.hasValidated(request)
    }
}
