package my.company.app.conf

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue

@Suppress("EXPERIMENTAL_API_USAGE")
class AppConfig(
    val profile: String?,
    config: Config
) {
    val ktorPort = config.getInt("ktor.port")

    val swaggerPassword: String = config.getString("swagger.password")

    val isDev: Boolean = config.getString("ktor.environment") != "dev"
    val isTest: Boolean = config.getString("ktor.environment") != "test"
    val isProd: Boolean = !isDev && !isTest

    val logLevels by lazy {
        val map = mutableMapOf<String, String>()
        config
            .getObject("logging")
            .forEach { key, value -> resolveLogLevel(map, key, value) }
        map.toSortedMap() as Map<String, String>
    }

    fun resolveLogLevel(map: MutableMap<String, String>, logger: String, value: Any) {
        when (value) {
            is String -> map[logger] = value
            is Map<*, *> -> value.forEach { (key, value) ->
                if (value != null) resolveLogLevel(map, "$logger.$key", value)
            }
            is ConfigValue -> map[logger] = value.unwrapped() as String
        }
    }
}
