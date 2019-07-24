package my.company.app.lib.ktor

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import my.company.app.conf.AppConfig
import my.company.app.lib.eager
import my.company.app.lib.logger
import org.koin.dsl.module
import org.koin.dsl.onClose

class HikariCPFeature {
    companion object Feature : ApplicationFeature<Application, Unit, HikariCPFeature> {
        private val logger = logger<HikariCPFeature>()
        private val KEY = AttributeKey<HikariCPFeature>("HikariCPFeature")
        override val key: AttributeKey<HikariCPFeature> get() = KEY

        fun config(appConfig: AppConfig) = with(appConfig.database) {
            logger.debug("Initializing Hikari config")
            val hikari = HikariConfig()
            hikari.username = username
            hikari.password = password
            hikari.driverClassName = driver
            hikari.jdbcUrl = jdbcUrl
            hikari.maximumPoolSize = poolMaxSize
            hikari.minimumIdle = poolMinIdle
            return@with hikari
        }

        fun hikari(appConfig: AppConfig): HikariPool {
            logger.debug("Initializing Hikari connection pool")
            return HikariPool(config(appConfig))
        }

        override fun install(pipeline: Application, configure: Unit.() -> Unit): HikariCPFeature {
            logger.debug("Installing HikariCPFeature")

            val feature = HikariCPFeature()

            pipeline.getKoin().modules(module {
                single { hikari(eager()) }.onClose {
                    it?.let {
                        logger.debug("Shutting down Hikari connection pool")
                        it.shutdown()
                    }
                }
            })

            return feature
        }
    }
}
