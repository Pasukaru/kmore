package my.company.app.web.controller.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.capture
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.user.GetUsersAction
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.singleValue
import my.company.app.web.controller.BaseWebControllerTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class WebGetUsersTest : BaseWebControllerTest(WebGetUsersLocation::class) {

    @Test
    fun returnsCorrectResult() = controllerTest {
        val actionMock = declareMock<GetUsersAction>()

        val actionRequest = captor<Unit>()
        val mockedActionResponse = (0 until 10).map { fixtures.user() }

        val expectedWebResponse = mockedActionResponse.map {
            WebGetUsersResponse(
                id = it.id,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName
            )
        }

        Mockito.doReturn(mockedActionResponse).`when`(actionMock).execute(capture(actionRequest))

        jsonGet {
            expectJsonResponseList(HttpStatusCode.OK, expectedWebResponse)
            assertThat(actionRequest.singleValue).isEqualTo(Unit)
        }
    }
}
