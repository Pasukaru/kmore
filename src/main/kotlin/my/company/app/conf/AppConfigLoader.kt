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

    fun loadProfile(profile: String? = null): AppConfig {
        val config = cache.getOrPut(profile) {
            val applicationConfig = loadFile("application.conf")
            val mergedConfig = profile?.let { applicationConfig.includeProfile(it) } ?: applicationConfig
            AppConfig(profile, mergedConfig)
        }

        if (config != currentConfig) {
            initLogging(config)
        }

        currentConfig = config
        return config
    }

    private fun loadFile(name: String): Config {
        if (FILE_CLASS_LOADER.exists(name)) return ConfigFactory.load(FILE_CLASS_LOADER, name)
        return ConfigFactory.load(SYSTEM_CLASS_LOADER, name)
    }

    private fun Config.includeProfile(name: String): Config {
        loadFile("application-$name.conf")
        val config = withFallback(loadFile("application-$name.conf"))
        return config.loadIncludes()
    }

    private fun Config.loadIncludes(): Config {
        val fallbackProfile = tryGetString("profile.include") ?: return this
        val fallbackConfig = loadFile("application-$fallbackProfile.conf")
        return withFallback(fallbackConfig.loadIncludes())
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
