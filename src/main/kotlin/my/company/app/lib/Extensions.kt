package my.company.app.lib

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

object Extensions {
    fun captureStackTrace(): Array<StackTraceElement> {
        val thisServiceName = this.javaClass.canonicalName
        val current = Thread.currentThread().stackTrace
            ?.filterNotNull()
            ?.toMutableList()
            // Remove Thread.getStackTrace()
            ?.let { it.subList(1, it.size) }
            // Remove calls to this service, we don't need them
            ?.filter { it.className != thisServiceName }
            // Add a hint that the next part of the stack trace is from another thread
            ?.let { arrayOf(StackTraceElement("Original", "Thread", "NoSource", 0)) + it }
            ?: emptyArray()
        return current
    }

    fun appendStackTrace(e: Throwable, history: Array<StackTraceElement>) {
        e.stackTrace += history
    }
}

