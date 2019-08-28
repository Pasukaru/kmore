package my.company.app.web.controller.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.capture
import io.ktor.http.HttpStatusCode
import my.company.app.business_logic.user.GetUsersAction
import my.company.app.business_logic.user.GetUsersFilter
import my.company.app.lib.Faker
import my.company.app.test.captor
import my.company.app.test.declareMock
import my.company.app.test.singleValue
import my.company.app.web.controller.BaseWebControllerTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant

class WebGetUsersTest : BaseWebControllerTest(WebGetUsersLocation::class) {

    @Test
    fun returnsCorrectResultWithFilter() = controllerTest {
        val actionMock = declareMock<GetUsersAction>()

        val actionRequest = captor<GetUsersFilter>()
        val mockedActionResponse = (0 until 10).map { fixtures.user() }

        val expectedWebResponse = mockedActionResponse.map {
            WebGetUsersResponse(
                id = it.id,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName
            )
        }

        val expectedFilter = GetUsersFilter(
            email = Faker.internet().emailAddress(),
            name = Faker.artist().name(),
            createdAtBefore = Instant.now()
        )

        Mockito.doReturn(mockedActionResponse).`when`(actionMock).execute(capture(actionRequest))

        jsonGet(query = parameterParser.toQuery(expectedFilter)) {
            expectJsonResponseList(HttpStatusCode.OK, expectedWebResponse)
            assertThat(actionRequest.singleValue).isEqualTo(expectedFilter)
        }
    }
}
