package my.company.app.lib.ktor

import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import my.company.app.conf.AppConfig
import my.company.app.lib.koin.eager
import my.company.app.lib.logger
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

class FlywayFeature {
    companion object Feature : ApplicationFeature<Application, Unit, FlywayFeature> {
        private val logger = logger<FlywayFeature>()
        private val KEY = AttributeKey<FlywayFeature>("HikariCPFeature")
        override val key: AttributeKey<FlywayFeature> get() = KEY

        fun config(appConfig: AppConfig): FluentConfiguration {
            return Flyway.configure()
                .locations("db.migration.public")
                .cleanDisabled(true)
                .schemas("public")
                .dataSource(HikariDataSource(HikariCPFeature.config(appConfig)))
        }

        fun flyway(config: FluentConfiguration) = Flyway(config)
        fun flyway(appConfig: AppConfig) = flyway(config(appConfig))

        override fun install(pipeline: Application, configure: Unit.() -> Unit): FlywayFeature {
            logger.debug("Installing HikariCPFeature")

            val feature = FlywayFeature()
            val appConfig = eager<AppConfig>()

            if (appConfig.flyway.enabled) {
                val flyway = flyway(appConfig)
                flyway.migrate()
            }

            return feature
        }
    }
}
