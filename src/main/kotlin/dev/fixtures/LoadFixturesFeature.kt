package dev.fixtures

import com.zaxxer.hikari.pool.HikariPool
import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import my.company.app.conf.AppConfig
import my.company.app.lib.DatabaseService
import my.company.app.lib.koin.eager
import my.company.app.lib.ktor.FlywayFeature.Feature.flyway
import my.company.app.lib.logger

@Suppress("MagicNumber")
class LoadFixturesFeature {
    private val tx = eager<DatabaseService>()
    private val fx = DbFixtures()

    companion object Feature : ApplicationFeature<Application, Unit, LoadFixturesFeature> {
        private val logger = logger<LoadFixturesFeature>()
        private val KEY = AttributeKey<LoadFixturesFeature>("LoadFixturesFeature")
        override val key: AttributeKey<LoadFixturesFeature> get() = KEY

        override fun install(pipeline: Application, configure: Unit.() -> Unit): LoadFixturesFeature {
            logger.debug("Loading fixtures")
            val start = System.currentTimeMillis()

            val feature = LoadFixturesFeature()
            val appConfig = eager<AppConfig>()
            val hikari = eager<HikariPool>()
            val connection = hikari.connection
            try {
                connection.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE")
            } finally {
                hikari.evictConnection(connection)
            }

            val flyway = flyway(appConfig)
            flyway.migrate()
            runBlocking { feature.loadFixtures() }

            logger.info("Fixtures loaded in ${System.currentTimeMillis() - start}ms")
            System.exit(0)

            return feature
        }
    }

    private suspend fun loadFixtures() = tx.transaction {
        fx.user(passwordClean = "test", email = "test@test.com")
        repeat(10) { fx.user() }
    }
}
