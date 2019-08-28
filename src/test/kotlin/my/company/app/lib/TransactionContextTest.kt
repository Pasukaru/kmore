package my.company.app.lib

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.runBlocking
import my.company.app.db.IsolationLevel
import my.company.app.test.expectNull
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.sql.Connection

class TransactionContextTest {

    @Test
    fun canCreateTransaction() = runBlocking {
        val connection = Mockito.mock(Connection::class.java)
        val dsl = Mockito.mock(DSLContext::class.java)

        Mockito.doReturn(1).`when`(dsl).execute(any<String>())

        val tx = TransactionContext(connection, dsl = dsl)
        coroutineContext[TransactionContext].expectNull()
        tx.transaction(isolationLevel = IsolationLevel.READ_COMMITTED, readOnly = false) {
            assertThat(coroutineContext[TransactionContext]).isSameAs(tx)
            assertThat(tx.status).isEqualTo(TransactionContext.Status.IN_TRANSACTION)
            assertThat(tx.isolationLevel).isEqualTo(IsolationLevel.READ_COMMITTED)
            assertThat(tx.readOnly).isEqualTo(false)
        }
        coroutineContext[TransactionContext].expectNull()
        assertThat(tx.status).isEqualTo(TransactionContext.Status.COMMITTED)
    }

    @Test
    fun canCreateNestedTransaction() = runBlocking {
        val connection = Mockito.mock(Connection::class.java)
        val dsl = Mockito.mock(DSLContext::class.java)

        Mockito.doReturn(1).`when`(dsl).execute(any<String>())

        val tx = TransactionContext(connection, dsl = dsl)
        val txInner = TransactionContext(connection, dsl = dsl)

        coroutineContext[TransactionContext].expectNull()
        tx.transaction(isolationLevel = IsolationLevel.READ_COMMITTED, readOnly = false) {
            assertThat(coroutineContext[TransactionContext]).isSameAs(tx)
            assertThat(tx.isolationLevel).isEqualTo(IsolationLevel.READ_COMMITTED)
            assertThat(tx.readOnly).isEqualTo(false)
            assertThat(tx.status).isEqualTo(TransactionContext.Status.IN_TRANSACTION)

            txInner.transaction(IsolationLevel.SERIALIZABLE, readOnly = true) {
                assertThat(coroutineContext[TransactionContext]).isSameAs(txInner)
                assertThat(txInner.isolationLevel).isEqualTo(IsolationLevel.SERIALIZABLE)
                assertThat(txInner.readOnly).isEqualTo(true)
                assertThat(txInner.status).isEqualTo(TransactionContext.Status.IN_TRANSACTION)

                // tx should be unchanged
                assertThat(tx.isolationLevel).isEqualTo(IsolationLevel.READ_COMMITTED)
                assertThat(tx.readOnly).isEqualTo(false)
                assertThat(tx.status).isEqualTo(TransactionContext.Status.IN_TRANSACTION)
            }

            // We should be back in the outer tx
            assertThat(coroutineContext[TransactionContext]).isSameAs(tx)

            // tx should still be unchanged
            assertThat(tx.isolationLevel).isEqualTo(IsolationLevel.READ_COMMITTED)
            assertThat(tx.readOnly).isEqualTo(false)
            assertThat(tx.status).isEqualTo(TransactionContext.Status.IN_TRANSACTION)
            assertThat(coroutineContext[TransactionContext]).isSameAs(tx)

            // txInner should have committed now
            assertThat(txInner.isolationLevel).isEqualTo(IsolationLevel.NONE)
            assertThat(txInner.readOnly).isEqualTo(false)
            assertThat(txInner.status).isEqualTo(TransactionContext.Status.COMMITTED)
        }

        // tx should have committed now
        coroutineContext[TransactionContext].expectNull()
        assertThat(tx.isolationLevel).isEqualTo(IsolationLevel.NONE)
        assertThat(tx.readOnly).isEqualTo(false)
        assertThat(tx.status).isEqualTo(TransactionContext.Status.COMMITTED)
    }
}
