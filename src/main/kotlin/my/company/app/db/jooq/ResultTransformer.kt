package my.company.app.db.jooq

import org.jooq.Record

interface ResultTransformer<T> {
    fun transform(record: Record): T
}
