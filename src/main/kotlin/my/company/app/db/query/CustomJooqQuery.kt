package my.company.app.db.query

import org.jooq.DSLContext
import org.jooq.ResultQuery

/**
 * Base class for custom queries.
 * All classes extending from this should be immutable.
 * The resulting JOOQ Query will be cached after the first time `buildInternal` has been called.
 * `buildInternal` will only be called once.
 */
abstract class CustomJooqQuery<T>(
    private val dsl: DSLContext
) {
    private var _jooqQuery: ResultQuery<*>? = null

    /**
     * Returns the cached JOOQ query.
     * If the JOOQ query has not been built yet, it will build and cache it beforehand.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun jooq(): ResultQuery<*> = _jooqQuery ?: with(dsl) { buildInternal().also { _jooqQuery = it } }

    /**
     * Executes the JOOQ query.
     * If the JOOQ query has not been built yet, it will build and cache it beforehand.
     */
    fun execute(): T = jooq().executeInternal()

    protected abstract fun DSLContext.buildInternal(): ResultQuery<*>
    protected abstract fun ResultQuery<*>.executeInternal(): T
}
