package my.company.app.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpStatusCode
import my.company.app.lib.eager
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.mockito.Mockito

fun HttpStatusCode?.expectCreated() = assertThat(this).isEqualTo(HttpStatusCode.Created)
fun HttpStatusCode?.expectOK() = assertThat(this).isEqualTo(HttpStatusCode.OK)

inline fun <reified BEAN : Any> declareSpy(qualifier: Qualifier? = null): BEAN {
    val bean = eager(BEAN::class, qualifier)
    val spy = Mockito.spy(bean)!!
    val koin = GlobalContext.get()
    koin.modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { spy } })
    return spy
}

inline fun <reified BEAN : Any> declareMock(qualifier: Qualifier? = null): BEAN {
    val spy = Mockito.mock(BEAN::class.java)!!
    val koin = GlobalContext.get()
    koin.modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { spy } })
    return spy
}
