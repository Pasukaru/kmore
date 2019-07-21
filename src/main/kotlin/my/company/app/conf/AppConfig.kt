package my.company.app.conf

import io.ktor.config.ApplicationConfig

@Suppress("EXPERIMENTAL_API_USAGE")
class AppConfig(config: ApplicationConfig) {
    val swaggerPassword: String = config.property("swagger.password").getString()
}
