package my.company.app.lib

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationFeature
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.util.pipeline.Pipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)
fun KClass<*>.logger(): Logger = LoggerFactory.getLogger(this.java)
fun Class<*>.logger(): Logger = LoggerFactory.getLogger(this)
fun logger(name: String): Logger = LoggerFactory.getLogger(name)

/**
 * Wraps a code block within a try/catch and calls catchException if an exception occurs.
 * @return The result of `block` if no error occurs. The result of `catchException` otherwise.
 */
inline fun <T, R> T.tryCatch(block: (T) -> R, catchException: (Throwable) -> R): R {
    return try {
        block(this)
    } catch (e: Throwable) {
        catchException(e)
    }
}

/**
 * Wraps the code block within a try/catch and returns null if an exception occurs.
 * @return The result of `block` if no error occurs. `null` otherwise.
 */
inline fun <T, R> T.tryOrNull(block: (T) -> R): R? = this.tryCatch(block) { null }

val Throwable.rootCause: Throwable
    get() {
        var root = this
        var cause = root.cause
        while (cause != null) {
            root = cause
            cause = cause.cause
        }
        return root
    }

fun <A : Pipeline<*, ApplicationCall>, B : Any, F : Any> A.getOrInstall(
    feature: ApplicationFeature<A, B, F>
): F {
    return featureOrNull(feature) ?: install(feature)
}
