package my.company.app.business_logic

import my.company.app.db.IsolationLevel
import org.junit.jupiter.api.Test

class BusinessLogicActionTest : AbstractActionTest() {

    private class TestAction : BusinessLogicAction<Unit, Unit>() {
        override suspend fun action(request: Unit) {}
    }

    @Test
    fun createsDefaultTransactionProperly() = actionTest {
        TestAction().execute(Unit)
        expectTransaction(isolationLevel = IsolationLevel.READ_COMMITTED, readOnly = false)
    }

    private class TestTransactionAction(tx: TransactionOptions) : BusinessLogicAction<Unit, Unit>(tx) {
        override suspend fun action(request: Unit) {}
    }

    @Test
    fun createsCustomTransactionProperly() = actionTest {
        var tx = TransactionOptions(
            isolationLevel = IsolationLevel.SERIALIZABLE,
            readOnly = false
        )
        TestTransactionAction(tx).execute(Unit)
        expectTransaction(calls = 1, isolationLevel = tx.isolationLevel, readOnly = tx.readOnly)

        tx = TransactionOptions(
            isolationLevel = IsolationLevel.READ_UNCOMMITTED,
            readOnly = true
        )
        TestTransactionAction(tx).execute(Unit)
        expectTransaction(calls = 2, isolationLevel = tx.isolationLevel, readOnly = tx.readOnly)
    }
}
