package my.company.app.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.runBlocking
import my.company.app.db.IsolationLevel
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.sql.Connection
import kotlin.test.fail

class DatabaseServiceTest {

    @Test
    fun canInheritTransaction(): Unit = runBlocking {
        val connection = Mockito.mock(Connection::class.java)
        val dsl = Mockito.mock(DSLContext::class.java)
        val svc = DatabaseService()

        Mockito.doReturn(1).`when`(dsl).execute(any<String>())

        val tx = TransactionContext(connection, dsl = dsl)
        tx.transaction(isolationLevel = IsolationLevel.SERIALIZABLE, readOnly = true) {
            svc.transaction(
                isolationLevel = IsolationLevel.SERIALIZABLE,
                readOnly = true,
                inherit = true
            ) {
                assertThat(coroutineContext[TransactionContext]).isSameAs(tx)
            }
        }
    }

    @Test
    fun cannotInheritTransactionWithDifferentIsolationlevel(): Unit = runBlocking {
        val connection = Mockito.mock(Connection::class.java)
        val dsl = Mockito.mock(DSLContext::class.java)
        val svc = DatabaseService()

        Mockito.doReturn(1).`when`(dsl).execute(any<String>())

        val tx = TransactionContext(connection, dsl = dsl)
        tx.transaction(isolationLevel = IsolationLevel.SERIALIZABLE, readOnly = true) {
            try {
                svc.transaction(
                    isolationLevel = IsolationLevel.READ_COMMITTED,
                    readOnly = true,
                    inherit = true
                ) {}
                fail("Expected error but got none")
            } catch (e: IllegalStateException) {
                assertThat(e.message).isEqualTo("Cannot inherit transaction with different isolation level: SERIALIZABLE != READ_COMMITTED")
            }
        }
    }

    @Test
    fun cannotInheritTransactionWithDifferentReadlevel(): Unit = runBlocking {
        val connection = Mockito.mock(Connection::class.java)
        val dsl = Mockito.mock(DSLContext::class.java)
        val svc = DatabaseService()

        Mockito.doReturn(1).`when`(dsl).execute(any<String>())

        val tx = TransactionContext(connection, dsl = dsl)
        tx.transaction(isolationLevel = IsolationLevel.SERIALIZABLE, readOnly = true) {
            try {
                svc.transaction(
                    isolationLevel = IsolationLevel.SERIALIZABLE,
                    readOnly = false,
                    inherit = true
                ) {}
                fail("Expected error but got none")
            } catch (e: IllegalStateException) {
                assertThat(e.message).isEqualTo("Cannot inherit transaction with different readonly property: true != false")
            }
        }
    }
}
