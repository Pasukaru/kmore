package my.company.app.web.controller.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.user.GetUsersAction
import my.company.app.db.newUser
import my.company.app.test.declareMock
import my.company.app.web.controller.BaseWebControllerTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class WebGetUsersTest : BaseWebControllerTest(WebGetUsersLocation::class) {

    @Test
    fun returnsCorrectResult() = controllerTest {
        val actionMock = declareMock<GetUsersAction>()

        val actionRequest = captor<Unit>()
        val mockedActionResponse = listOf(newUser(
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            password = "password"
        ))

        val expectedWebResponse = mockedActionResponse.map {
            WebGetUsersResponse(
                id = it.id,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName
            )
        }

        Mockito.doReturn(mockedActionResponse).`when`(actionMock).execute(actionRequest.capture())

        jsonGet {
            expectJsonResponseList(HttpStatusCode.OK, expectedWebResponse)
            assertThat(actionRequest.value).isEqualTo(Unit)
        }
    }
}
