package my.company.app.db.jooq

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import my.company.app.lib.logger
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.dsl.onClose

class HikariCPFeature {

    lateinit var pool: HikariPool

    companion object Feature : ApplicationFeature<Application, KoinApplication, HikariCPFeature> {
        private const val MIN_IDLE = 10
        private const val MAX_POOL_SIZE = 20
        private val logger = logger<HikariCPFeature>()

        override val key: AttributeKey<HikariCPFeature> get() = AttributeKey("DSLContext")

        override fun install(pipeline: Application, configure: (KoinApplication).() -> Unit): HikariCPFeature {
            logger.debug("Installing HikariCPFeature")

            val feature = HikariCPFeature()

            GlobalContext.get().modules(module {
                single {
                    logger.debug("Initializing Hikari config")
                    val config = HikariConfig()
                    config.username = "my_project"
                    config.password = "my_project"
                    config.driverClassName = org.postgresql.Driver::class.qualifiedName
                    config.jdbcUrl = "jdbc:postgresql://localhost:10001/my_project"
                    config.minimumIdle = MIN_IDLE
                    config.maximumPoolSize = MAX_POOL_SIZE

                    logger.debug("Initializing Hikari connection pool")
                    val pool = HikariPool(config)
                    feature.pool = pool

                    pool
                }.onClose {
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
