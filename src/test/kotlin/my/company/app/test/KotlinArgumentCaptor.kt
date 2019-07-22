package my.company.app.test

import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import kotlin.reflect.KClass

class KotlinArgumentCaptor<TYPE: Any>(clazz: KClass<TYPE>) {

    private val actual: ArgumentCaptor<TYPE> = ArgumentCaptor.forClass(clazz.java)
    private val mock: TYPE = Mockito.mock(clazz.java)!!

    fun capture(): TYPE {
        actual.capture()
        return mock
    }

    val value: TYPE? get() = actual.value
    val allValues: List<TYPE> get() = actual.allValues

    companion object {
        inline fun <reified TYPE: Any> forClass() = KotlinArgumentCaptor(TYPE::class)
    }

    override fun toString(): String {
        return "${this::class.simpleName}[${mock::class}]"
    }
}
