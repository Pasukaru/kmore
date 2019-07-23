package my.company.app.test

import org.jooq.Record

class DbFixtures : AbstractFixtures() {
    override fun afterInit(record: Record) {}
}
