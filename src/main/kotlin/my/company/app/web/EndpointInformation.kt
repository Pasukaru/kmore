package my.company.app.web

import io.ktor.http.HttpMethod
import io.ktor.util.AttributeKey
import kotlin.reflect.KClass

data class EndpointInformation(
    val method: HttpMethod,
    val locationClass: KClass<*>,
    val path: String = getPathFromLocation(locationClass)
) {
    companion object Attribute {
        val key: AttributeKey<EndpointInformation> = AttributeKey(EndpointInformation::class.java.canonicalName)
    }

    override fun toString(): String {
        val str = StringBuilder(this::class.simpleName)
            .append("{")
            .append(method.value)
            .append(" ")
            .append(path)

        str.append("}")

        return str.toString()
    }
}
