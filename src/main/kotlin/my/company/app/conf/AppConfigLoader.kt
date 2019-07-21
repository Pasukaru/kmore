package my.company.app.conf

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.config.tryGetString
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.Collections
import java.util.Enumeration

@Suppress("EXPERIMENTAL_API_USAGE")
object AppConfigLoader {
    private val SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader()
    private val FILE_CLASS_LOADER = FileClassLoader()

    fun load(): AppConfig {
        val applicationConfig = load("application.conf")
        val includeProfile = System.getenv("PROFILE")?.takeIf { it.isNotBlank() }
        val mergedConfig = includeProfile?.let { applicationConfig.includeProfile(it) } ?: applicationConfig
        return AppConfig(mergedConfig)
    }

    private fun load(name: String): Config {
        if (FILE_CLASS_LOADER.exists(name)) return ConfigFactory.load(FILE_CLASS_LOADER, name)
        return ConfigFactory.load(SYSTEM_CLASS_LOADER, name)
    }

    private fun Config.includeProfile(name: String): Config {
        load("application-$name.conf")
        val config = withFallback(load("application-$name.conf"))
        return config.loadIncludes()
    }

    private fun Config.loadIncludes(): Config {
        val fallbackProfile = tryGetString("profile.include") ?: return this
        val fallbackConfig = load("application-$fallbackProfile.conf")
        return withFallback(fallbackConfig.loadIncludes())
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
