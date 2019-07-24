package my.company.app.test

import my.company.app.conf.AppConfigLoader.initLogging
import my.company.app.initConfig
import org.junit.jupiter.api.BeforeAll

abstract class AbstractTest {

    companion object {
        private val lock = Any()
        private var started = false

        @BeforeAll
        @JvmStatic
        @Suppress("unused")
        private fun globalSetup() {
            if (!started) {
                synchronized(lock) {
                    if (!started) {
                        val config = initConfig("test")
                        initLogging(config)
                        started = true
                    }
                }
            }
        }
    }
}
