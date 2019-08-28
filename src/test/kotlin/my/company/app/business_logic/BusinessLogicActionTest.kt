package my.company.app.business_logic

import my.company.app.db.IsolationLevel
import my.company.app.lib.TransactionContext
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.coroutineContext

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

    @RepeatedTest(10)
    fun createsCustomTransactionProperly() = actionTest {
        val tx = TransactionOptions(
            isolationLevel = IsolationLevel.values().random(),
            readOnly = Math.random() > 0.5
        )
        TestTransactionAction(tx).execute(Unit)
        expectTransaction(isolationLevel = tx.isolationLevel, readOnly = tx.readOnly)
    }

    private class TxCaptureAction : BusinessLogicAction<Unit, Unit>() {
        var tx: TransactionContext? = null
            private set

        override suspend fun action(request: Unit) {
            tx = coroutineContext[TransactionContext]
        }
    }
}
