package my.company.app.lib.ktor

import io.ktor.features.DataConversion
import io.ktor.util.DataConversionException
import java.util.UUID

fun DataConversion.Configuration.uuidConverter() {
    convert<UUID> {
        decode { values, _ -> values.singleOrNull()?.let(UUID::fromString) }
        encode { value ->
            when (value) {
                null -> listOf()
                is UUID -> listOf(value.toString())
                else -> throw DataConversionException("Cannot convert $value to UUID")
            }
        }
    }
}
