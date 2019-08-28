package my.company.app.test

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.ktor.http.HttpStatusCode
import my.company.app.lib.koin.KoinContext
import my.company.app.lib.koin.eager
import my.company.app.lib.koin.singleInstance
import my.company.app.lib.validation.Email
import my.company.app.lib.validation.NotBlank
import my.company.app.lib.validation.Password
import org.jooq.Record
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.opentest4j.AssertionFailedError
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField

fun <T> Collection<T>.expectOne(): T {
    assertThat(size).isEqualTo(1)
    return first()
}

fun HttpStatusCode?.expectCreated() = assertThat(this).isEqualTo(HttpStatusCode.Created)
fun HttpStatusCode?.expectOK() = assertThat(this).isEqualTo(HttpStatusCode.OK)

inline fun <reified BEAN : Any> declareSpy(qualifier: Qualifier? = null): BEAN {
    val bean = eager(BEAN::class, qualifier)
    val spy = Mockito.spy(bean)!!
    KoinContext.getOrError().modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { spy } })
    return spy
}

inline fun <reified BEAN : Any> declareMock(qualifier: Qualifier? = null): BEAN {
    val mock = Mockito.mock(BEAN::class.java)!!
    KoinContext.getOrError().modules(module { single(override = true, createdAtStart = true, qualifier = qualifier) { mock } })
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

val <T> ArgumentCaptor<T>.singleValue: T
    get() {
        assertThat(allValues).hasSize(1)
        return value
    }

inline fun <reified TYPE : Any?> captor(): ArgumentCaptor<TYPE> = ArgumentCaptor.forClass(TYPE::class.java)

suspend inline fun <reified T : Throwable> expectException(crossinline fn: suspend () -> Unit): T = expectException(T::class) { fn() }
@Suppress("UNCHECKED_CAST")
suspend fun <T : Throwable> expectException(expected: KClass<T>, fn: suspend () -> Unit): T {
    try {
        fn()
    } catch (e: Throwable) {
        if (e::class.isSubclassOf(expected)) return e as T
        throw AssertionFailedError("Expected $expected to be thrown, but got ${e::class} ($e)", e)
    }
    throw AssertionFailedError("Expected $expected to be thrown, but nothing was thrown.")
}

fun Record.expectAllUnchanged() {
    this.fields().forEach { expectUnchanged(it.name) }
}

fun Record.expectUnchanged(field: String) {
    val changed = this.changed(field)
    assertThat(changed, "Expected $field to not have been changed").isFalse()
}

fun Record.expectAllChanged() {
    this.fields().forEach { expectChanged(it.name) }
}

fun Record.expectChanged(field: String) {
    // Null fields are not marked as changed on init
    val changed = this.changed(field) || this[field] == null
    assertThat(changed, "Expected $field to have been changed").isTrue()
}

fun <T : Any> T?.expectNotNull(name: String? = null): T {
    assertThat(this, name).isNotNull()
    return this!!
}

fun <T : Any> T?.expectNull(name: String? = null): T? {
    assertThat(this, name).isNull()
    return null
}

fun Boolean.expectTrue(name: String? = null): Boolean {
    assertThat(this, name).isTrue()
    return this
}

fun Boolean.expectFalse(name: String? = null): Boolean {
    assertThat(this, name).isFalse()
    return this
}

fun expectNotNullProperty(property: KProperty<*>) {
    assertThat(property.returnType.isMarkedNullable, "Property $property is not optional and should not be nullable").isFalse()
}

fun expectEmailValidation(property: KProperty<String>, optional: Boolean = false) {
    val annotation = property.javaField?.getAnnotation(Email::class.java)
    assertThat(annotation, "Expected @Password annotation on $property").isNotNull()
    if (!optional) expectNotNullProperty(property)
}

fun expectNotBlankValidation(property: KProperty<String>, optional: Boolean = false) {
    val annotation = property.javaField?.getAnnotation(NotBlank::class.java)
    assertThat(annotation, "Expected @NotBlank annotation on $property").isNotNull()
    if (!optional) expectNotNullProperty(property)
}

fun expectPasswordValidation(property: KProperty<String>, optional: Boolean = false) {
    val annotation = property.javaField?.getAnnotation(Password::class.java)
    assertThat(annotation, "Expected @Email annotation on $property").isNotNull()
    if (!optional) expectNotNullProperty(property)
}
