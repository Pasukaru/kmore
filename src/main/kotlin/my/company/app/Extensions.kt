package my.company.app

import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.Options
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> inject(qualifier: Qualifier? = null): Lazy<T> {
    return lazy { GlobalContext.get().koin.get<T>(T::class, qualifier, null) }
}

fun <T : Any> Scope.createInstance(
    clazz: KClass<T>
): T {
    val ctor =
        clazz.primaryConstructor ?: throw IllegalStateException("Cannot instanciate class without primary ctor: $clazz")

    val params = ctor.parameters.associateWith {
        val paramClass = it.type.classifier as? KClass<*>
            ?: throw IllegalStateException("Cannot instanciate class with unclassified primary ctor parameters: $clazz $ctor")
        get<Any>(paramClass, null, null)
    }

    params.forEach {
        println(it.value::class.java)
    }

    val instance = ctor.callBy(params)

    return instance
}

@Suppress("unused")
inline fun <reified T : Any> KClass<T>.containerModule(): Module = module { singleContainer<T>() }

inline fun <reified T : Any> Module.singleContainer() {
    T::class.declaredMemberProperties.forEach {
        val clazz = it.returnType.classifier as KClass<*>
        singleInstance(clazz)
    }

    singleInstance<T>()
}

inline fun <reified T : Any> Module.singleInstance(options: Options = Options()) =
    singleInstance(T::class, options)

fun <T : Any> Module.singleInstance(clazz: KClass<T>, options: Options = Options()) {
    val bean = BeanDefinition<Any>(null, null, clazz)
    bean.definition = { createInstance(clazz) }
    bean.kind = Kind.Single
    this.declareDefinition(bean, options)
}


//<editor-fold desc="Koin Extensions">
private val KoinLogger = LoggerFactory.getLogger(PackageNoOp::class.java.packageName + ".Koin")

fun Koin.resolveParameters(fn: KFunction<*>): Map<KParameter, Any> {
    return fn.parameters.associateWith { parameter ->
        KoinLogger.trace("Resolving parameter: $parameter")
        val type = parameter.type.classifier as KClass<*>
        get<Any>(type, null, null)
    }
}

fun <T : Any> Koin.instanciate(type: KClass<T>): T {
    val ctor = type.primaryConstructor
        ?: throw IllegalStateException("No primary constructor present for $type")
    val parameters = resolveParameters(ctor)
    return ctor.callBy(parameters)
}
//</editor-fold>