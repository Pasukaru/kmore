package my.company.app.db.repo

abstract class AbstractCRUDTest : AbstractRepositoryTest() {
    abstract fun canInsert()
    abstract fun canFindById()
    abstract fun canFindAll()
    abstract fun canUpdate()
    abstract fun canDeleteById()
}
