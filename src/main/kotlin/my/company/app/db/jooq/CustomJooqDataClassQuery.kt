package my.company.app.db.jooq

import kotlin.reflect.KClass

abstract class CustomJooqDataClassQuery<T : Any>(
    protected val dataClass: KClass<T>
) : CustomJooqQuery() {
    open fun getResultTransformer(): ResultTransformer<T> = DefaultDataClassMapper.forClass(dataClass)
}
