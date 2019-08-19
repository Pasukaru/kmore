package my.company.app.lib.ktor

import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import my.company.app.lib.InvalidInstantException
import org.hibernate.validator.internal.engine.path.PathImpl
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class ParameterParser {
    inline fun <reified T : Any> parse(parameters: Parameters) = parse(parameters, T::class)
    fun <T : Any> parse(parameters: Parameters, type: KClass<T>): T {
        if (!type.isData) error("Cannot parse parameters as result class $type is not a data class")
        val ctor = type.primaryConstructor ?: error("Cannot parse parameters as result class $type does not have a primary constructor")

        val ctorParams = ctor.parameters.associate { parameter ->
            val value = parameter.name?.let { parseValue(parameters[it], parameter) }
            parameter to value
        }

        return ctor.callBy(ctorParams)
    }

    fun toQuery(parameters: Any): String {
        return "?" + formUrlEncode(parameters)
    }

    private fun parseValue(value: String?, parameter: KParameter): Any? {
        val type = parameter.type.classifier ?: error("Missing query parameter type for parameter: $parameter")

        value ?: return null

        return when (type) {
            String::class -> value
            Boolean::class -> value.trim().toBoolean()
            Int::class -> value.trim().toInt()
            Long::class -> value.trim().toLong()
            Float::class -> value.trim().toFloat()
            Double::class -> value.trim().toDouble()
            Instant::class -> {
                val trimmed = value.trim()
                try {
                    Instant.parse(trimmed)
                } catch (e: DateTimeParseException) {
                    throw InvalidInstantException(value = trimmed, propertyPath = PathImpl.createRootPath().also { it.addParameterNode(parameter.name, parameter.index) })
                }
            }
            else -> error("Unsupported query parameter type: $type")
        }
    }

    private fun formUrlEncode(parameters: Any): String {
        val params = Parameters.build {
            parameters::class.declaredMemberProperties.forEach { property ->
                val name = property.name
                @Suppress("MoveVariableDeclarationIntoWhen")
                val value = property.getter.call(parameters) ?: return@forEach
                val strValue = when (value) {
                    is String -> value
                    is Boolean,
                    is Int,
                    is Long,
                    is Float,
                    is Double,
                    is Instant -> value.toString()
                    else -> error("Unsupported query parameter: ($name: $value) $property")
                }
                append(name, strValue)
            }
        }
        return params.formUrlEncode()
    }
}
