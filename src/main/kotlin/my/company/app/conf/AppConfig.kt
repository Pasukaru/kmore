package my.company.app.conf

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue

@Suppress("EXPERIMENTAL_API_USAGE")
class AppConfig(
    val profile: String?,
    private val config: Config
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

    val database = Database()

    inner class Database {
        val host: String = config.getString("database.host")
        val port: Int = config.getInt("database.port")
        val name: String = config.getString("database.name")
        val driver: String = config.getString("database.driver")
        val username: String = config.getString("database.username")
        val password: String = config.getString("database.password")
        val jdbcUrl: String = "jdbc:postgresql://$host:$port/$name"
        val poolMaxSize: Int = config.getInt("database.poolMaxSize")
        val poolMinIdle: Int = config.getInt("database.poolMinIdle")
    }

    val flyway = Flyway()

    inner class Flyway {
        val enabled: Boolean = config.getBoolean("flyway.enabled")
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
