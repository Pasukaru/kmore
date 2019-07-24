package my.company.app.conf

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.config.tryGetString
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.Collections
import java.util.Enumeration

@Suppress("EXPERIMENTAL_API_USAGE")
object AppConfigLoader {
    private val SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader()
    private val FILE_CLASS_LOADER = FileClassLoader()
    private val cache = mutableMapOf<String?, AppConfig>()
    private var currentConfig: AppConfig? = null

    private val applicationConfig: Config by lazy { loadFile("application.conf") }

    fun loadProfile(profile: String? = null): AppConfig {
        val config = cache.getOrPut(profile) {
            AppConfig(profile, profile?.let(::loadFullProfile) ?: applicationConfig)
        }
        if (currentConfig != config) initLogging(config)
        currentConfig = config
        return config
    }

    private fun loadFile(name: String): Config {
        if (FILE_CLASS_LOADER.exists(name)) return ConfigFactory.load(FILE_CLASS_LOADER, name)
        return ConfigFactory.load(SYSTEM_CLASS_LOADER, name)
    }

    private fun loadFullProfile(profile: String): Config {
        val configs = mutableListOf<Config>()
        var current: Config? = loadFile("application-$profile.conf")
        while (current != null) {
            configs.add(current)
            current = current.loadInclude()
        }
        configs.add(applicationConfig)
        return configs.reduce { a, b -> a.withFallback(b) }
    }

    private fun Config.loadInclude(): Config? {
        val profile = tryGetString("profile.include") ?: return null
        return loadFile("application-$profile.conf")
    }

    private fun initLogging(appConfig: AppConfig) {
        val fac = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger = fac.getLogger("ROOT")

        fac.loggerList.forEach {
            if (it != rootLogger) {
                it.level = null
            }
        }

        appConfig.logLevels.forEach { (key, value) ->
            fac.getLogger(key).level = Level.valueOf(value)
        }
    }

    private class FileClassLoader : ClassLoader() {
        override fun getResource(name: String?): URL {
            return File(name).toURI().toURL()
        }

        override fun getResourceAsStream(name: String?): InputStream {
            return File(name).inputStream()
        }

        override fun getResources(name: String?): Enumeration<URL> {
            return Collections.enumeration(listOf(getResource(name)))
        }

        fun exists(name: String): Boolean {
            val file = File(name)
            return file.exists() && file.isFile
        }
    }
}
