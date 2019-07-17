package my.company.app.db.jooq

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStopping
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.Options
import org.koin.dsl.module
import org.slf4j.LoggerFactory

class HikariCPFeature {

    lateinit var pool: HikariPool

    companion object Feature : ApplicationFeature<Application, KoinApplication, HikariCPFeature> {
        private const val MIN_IDLE = 10
        private const val MAX_POOL_SIZE = 20
        private val logger = LoggerFactory.getLogger(HikariCPFeature::class.java)

        override val key: AttributeKey<HikariCPFeature> get() = AttributeKey("DSLContext")

        override fun install(pipeline: Application, configure: (KoinApplication).() -> Unit): HikariCPFeature {
            logger.debug("Installing HikariCPFeature")

            val feature = HikariCPFeature()

            GlobalContext.get().modules(module {
                val bean = BeanDefinition<Any>(null, null, HikariPool::class)

                bean.definition = {
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

                    pipeline.environment.monitor.subscribe(ApplicationStopping) {
                        logger.debug("Shutting down Hikari connection pool")
                        pool.shutdown()
                    }

                    pool
                }

                bean.kind = Kind.Single
                this.declareDefinition(bean, Options())
            })

            return feature
        }
    }

}
