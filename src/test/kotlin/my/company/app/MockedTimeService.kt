package my.company.app

import my.company.app.lib.TimeService
import org.mockito.Mockito
import java.time.Instant

object MockedTimeService {
    @Suppress("MemberVisibilityCanBePrivate")
    val start: Instant = Instant.now()
    val mock: TimeService
        get() {
            return Mockito.mock(TimeService::class.java).apply {
                Mockito.doReturn(start).`when`(this).now()
            }
        }
}
