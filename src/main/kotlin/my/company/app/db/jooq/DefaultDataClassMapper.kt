package my.company.app.db.jooq

import my.company.app.lib.EnumParser
import org.jooq.Record
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

open class DefaultDataClassMapper<T : Any>(private val clazz: KClass<T>) : ResultTransformer<T> {

    private val members = clazz.declaredMemberProperties.associateBy { it.name }
    private val ctorParameters = clazz.constructors.first().parameters

    override fun transform(record: Record): T {
        val ctor = clazz.constructors.first()
        val ctorAccessible = ctor.isAccessible

        val row = record.fields().associate { it.name to it.getValue(record) }

        val parameters = ctorParameters.map { param ->
            if (!row.containsKey(param.name)) throw UninitializedPropertyAccessException("Member not found for ${param.name}")

            val value = row[param.name]

            parseParameter(param, value)
        }.toMap()

        parameters.forEach { (param, value) ->
            if (!param.type.isMarkedNullable && value == null) {
                throw UninitializedPropertyAccessException("Param $param is not marked as nullable, but value from result set is null!")
            }
        }

        return ctor
            .also { if (!ctorAccessible) ctor.isAccessible = true }
            .callBy(parameters)
            .also { if (!ctorAccessible) ctor.isAccessible = false }
    }

    @Suppress("ComplexMethod")
    protected fun parseParameter(param: KParameter, value: Any?): Pair<KParameter, Any?> {
        val classifier = param.type.classifier!!

        val member = members[param.name]
            ?: throw UninitializedPropertyAccessException("Member not found for ${param.name}")

        return param to when {
            classifier == String::class -> {
                when (value) {
                    null -> null
                    is String -> value
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as String")
                }
            }
            classifier == Int::class -> {
                when (value) {
                    null -> null
                    is Int -> value
                    is Number -> value.toInt()
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Int")
                }
            }
            classifier == Long::class -> {
                when (value) {
                    null -> null
                    is Long -> value
                    is Number -> value.toLong()
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Long")
                }
            }
            classifier == Double::class -> {
                when (value) {
                    null -> null
                    is Double -> value
                    is Number -> value.toDouble()
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Double")
                }
            }
            classifier == Boolean::class -> {
                when (value) {
                    null -> null
                    is Boolean -> value
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Boolean")
                }
            }
            classifier == BigDecimal::class -> {
                when (value) {
                    null -> null
                    is BigDecimal -> value
                    is Long -> BigDecimal.valueOf(value)
                    is Int -> BigDecimal.valueOf(value.toLong())
                    is Double -> BigDecimal.valueOf(value)
                    is Float -> BigDecimal.valueOf(value.toDouble())
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as BigDecimal")
                }
            }
            classifier == UUID::class -> {
                when (value) {
                    null -> null
                    is UUID -> value
                    is String -> UUID.fromString(value)
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as UUID")
                }
            }
            classifier == Instant::class -> {
                when (value) {
                    null -> null
                    is Instant -> value
                    is Timestamp -> value.toInstant()
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Instant")
                }
            }/*
            classifier == TimestampRange::class -> {
                when (value) {
                    null -> null
                    is TimestampRange -> value
                    is String -> TimestampRangeConverter.INSTANCE.convertToEntityAttribute(value)
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as TimestampRange")
                }
            }*/
            (classifier as? KClass<*>)?.java?.isEnum == true -> {
                when (value) {
                    null -> null
                    is String -> EnumParser.parseGenericEnum(classifier.java, value)
                    value.javaClass == classifier.java -> value
                    else -> throw IllegalArgumentException("Unable to process $value for $param (${value.javaClass}) as Enum ($classifier)")
                }
            }
            else -> throw UninitializedPropertyAccessException("Cannot parse type ${member.javaClass}($classifier) of property ${param.name}")
        }
    }

    init {
        if (!clazz.isData) {
            throw IllegalArgumentException("Class $clazz is not a data class")
        }
    }

    companion object {
        private val CACHE = mutableMapOf<KClass<*>, DefaultDataClassMapper<*>>()

        fun <T : Any> forClass(clazz: KClass<T>): DefaultDataClassMapper<T> {
            @Suppress("UNCHECKED_CAST")
            return CACHE.getOrPut(clazz) { DefaultDataClassMapper(clazz) } as DefaultDataClassMapper<T>
        }
    }
}
