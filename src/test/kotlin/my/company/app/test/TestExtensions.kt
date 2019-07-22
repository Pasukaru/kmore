package my.company.app.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpStatusCode
import my.company.app.lib.eager
import my.company.app.lib.singleInstance
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.mockito.Mockito
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

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
    val mock = Mockito.mock(BEAN::class.java)!!
    val koin = GlobalContext.get()
    koin.modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { mock } })
    return mock
}

inline fun <reified T : Any> mockedContainerModule(): Module = module { mockSingleContainer<T>() }

inline fun <reified T : Any> Module.mockSingleContainer() {
    T::class.declaredMemberProperties.forEach { property ->
        mockedSingleInstance(property.returnType.classifier as KClass<*>)
    }
    singleInstance<T>()
}

fun <T : Any> Module.mockedSingleInstance(clazz: KClass<T>, options: Options = Options(isCreatedAtStart = true)) {
    val bean = BeanDefinition<Any>(null, null, clazz)
    bean.definition = { Mockito.mock(clazz.java) }
    bean.kind = Kind.Single
    this.declareDefinition(bean, options)
}
