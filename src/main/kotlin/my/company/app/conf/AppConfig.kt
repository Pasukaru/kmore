package my.company.app.conf

import com.typesafe.config.Config

@Suppress("EXPERIMENTAL_API_USAGE")
class AppConfig(config: Config) {
    val ktorPort = config.getInt("ktor.port")

    val swaggerPassword: String = config.getString("swagger.password")
    val isDev: Boolean = config.getString("ktor.environment") != "dev"
    val isTest: Boolean = config.getString("ktor.environment") != "test"
    val isProd: Boolean = !isDev && !isTest
}
